package me.desht.modularrouters.block.tile;

import com.google.common.collect.Sets;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.DetectorModule.SignalType;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.Upgrade;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityItemRouter extends TileEntity implements ITickable, IInventory {
    public static final int N_MODULE_SLOTS = 9;
    public static final int N_UPGRADE_SLOTS = 4;

    public static final int COMPILE_MODULES = 0x01;
    public static final int COMPILE_UPGRADES = 0x02;

    private int counter = 0;

    private RouterRedstoneBehaviour redstoneBehaviour = RouterRedstoneBehaviour.ALWAYS;

    private final ItemStackHandler bufferHandler = new ItemStackHandler(1) {
        @Override
        public void onContentsChanged(int slot) {
            getWorld().updateComparatorOutputLevel(getPos(), getBlockType());
        }
    };
    private final ItemStackHandler modulesHandler = new RouterItemHandler.ModuleHandler(this);
    private final ItemStackHandler upgradesHandler = new RouterItemHandler.UpgradeHandler(this);

    private final CombinedInvWrapper joined = new CombinedInvWrapper(bufferHandler, modulesHandler, upgradesHandler);

    private final List<CompiledModule> compiledModules = new ArrayList<>();
    private byte recompileNeeded = COMPILE_MODULES | COMPILE_UPGRADES;
    private int tickRate = Config.baseTickRate;
    private int itemsPerTick = 1;
    private final int[] upgradeCount = new int[ItemUpgrade.UpgradeType.values().length];

    // for tracking redstone emission levels for the detector module
    private final int SIDES = EnumFacing.values().length;
    private final int[] redstoneLevels = new int[SIDES];
    private final int[] newRedstoneLevels = new int[SIDES];
    private final SignalType[] signalType = new SignalType[SIDES];
    private final SignalType[] newSignalType = new SignalType[SIDES];
    private boolean canEmit, prevCanEmit; // used if 1 or more detector modules are installed

    // when a player wants to configure an installed module, this tracks the slot
    // number received from the client-side GUI for that player
    private final Map<UUID, Integer> playerToSlot = new HashMap<>();

    private int lastPower;  // tracks previous redstone power level for pulse mode

    private boolean active;  // tracks active state of router
    private int activeTimer = 0;  // used in PULSE mode to time out the active state

    private final Set<UUID> permitted = Sets.newHashSet(); // permitted user ID's from security upgrade

    // bitmask of which of the 6 sides are currently open
    private byte sidesOpen;

    // track eco-mode
    private boolean ecoMode = false;
    private int ecoCounter = Config.ecoTimeout;

    public TileEntityItemRouter() {
        super();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("tile.itemRouter.name");
    }

    public IItemHandler getBuffer() {
        return bufferHandler;
    }

    public IItemHandler getModules() {
        return modulesHandler;
    }

    public IItemHandler getUpgrades() {
        return upgradesHandler;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbtTagCompound = super.getUpdateTag();
        writeToNBT(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("Active", active);
        compound.setByte("Sides", sidesOpen);
        compound.setBoolean("Eco", ecoMode);
        return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        boolean newActive = pkt.getNbtCompound().getBoolean("Active");
        byte newSidesOpen = pkt.getNbtCompound().getByte("Sides");
        boolean newEco = pkt.getNbtCompound().getBoolean("Eco");
        setActive(newActive);
        setSidesOpen(newSidesOpen);
        setEcoMode(newEco);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return (oldState.getBlock() != newState.getBlock());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, EnumFacing side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, side);
    }

    @Nonnull
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(joined);
            } else {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(bufferHandler);
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        bufferHandler.deserializeNBT(nbt.getCompoundTag("Buffer"));
        modulesHandler.deserializeNBT(nbt.getCompoundTag("Modules"));
        upgradesHandler.deserializeNBT(nbt.getCompoundTag("Upgrades"));
        try {
            redstoneBehaviour = RouterRedstoneBehaviour.valueOf(nbt.getString("Redstone"));
        } catch (IllegalArgumentException e) {
            // shouldn't ever happen...
            redstoneBehaviour = RouterRedstoneBehaviour.ALWAYS;
        }
        active = nbt.getBoolean("Active");
        activeTimer = nbt.getInteger("ActiveTimer");
        ecoMode = nbt.getBoolean("EcoMode");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt = super.writeToNBT(nbt);
        nbt.setTag("Buffer", bufferHandler.serializeNBT());
        nbt.setTag("Modules", modulesHandler.serializeNBT());
        nbt.setTag("Upgrades", upgradesHandler.serializeNBT());
        nbt.setString("Redstone", redstoneBehaviour.name());
        nbt.setBoolean("Active", active);
        nbt.setInteger("ActiveTimer", activeTimer);
        nbt.setBoolean("EcoMode", ecoMode);
        return nbt;
    }

    @Override
    public void update() {
        if (recompileNeeded != 0) {
            compile();
        }

        if (getWorld().isRemote) {
            return;
        }

        counter++;

        if (getRedstoneBehaviour() == RouterRedstoneBehaviour.PULSE) {
            int power = getWorld().isBlockIndirectlyGettingPowered(getPos());
            // we need to use the real tick rate here, not the possibly eco-mode tick rate that getTickRate() returns
            if (power > lastPower && counter >= tickRate) {
                executeModules();
                counter = 0;
                if (active) {
                    activeTimer = tickRate;
                }
            }
            // need to turn the state inactive after a short time...
            if (activeTimer > 0) {
                if (--activeTimer == 0) {
                    setActive(false);
                }
            }
            lastPower = power;
        } else {
            if (counter >= getTickRate()) {
                executeModules();
                counter = 0;
            }
        }

        if (ecoMode) {
            if (active) {
                ecoCounter = Config.ecoTimeout;
            } else if (ecoCounter > 0) {
                ecoCounter--;
            }
        }
    }

    private void executeModules() {
        boolean newActive = false;

        if (redstoneModeAllowsRun()) {
            if (prevCanEmit || canEmit) {
                Arrays.fill(newRedstoneLevels, 0);
                Arrays.fill(newSignalType, SignalType.NONE);
            }
            for (CompiledModule mod : compiledModules) {
                if (mod != null && mod.execute(this)) {
                    newActive = true;
                    if (mod.termination()) {
                        break;
                    }
                }
            }
            if (prevCanEmit || canEmit) {
                handleRedstoneEmission();
            }
        }
        if (newActive != active) {
            setActive(newActive);
        }
        prevCanEmit = canEmit;
    }

    public int getTickRate() {
        return ecoMode && ecoCounter == 0 ? Config.lowPowerTickRate : tickRate;
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public void setRedstoneBehaviour(RouterRedstoneBehaviour redstoneBehaviour) {
        this.redstoneBehaviour = redstoneBehaviour;
        if (redstoneBehaviour == RouterRedstoneBehaviour.PULSE) {
            lastPower = getWorld().isBlockIndirectlyGettingPowered(getPos());
        }
    }

    /**
     * Check if the router processed anything on its last tick
     *
     * @return true if the router processed something
     */
    public boolean isActive() {
        return active;
    }

    private void setActive(boolean newActive) {
        if (active != newActive) {
            active = newActive;
            if (!worldObj.isRemote) {
                sendBlockstateToClients();
            } else {
                worldObj.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    public boolean isSideOpen(Module.RelativeDirection side) {
        return (sidesOpen & side.getMask()) != 0;
    }

    private void setSidesOpen(byte sidesOpen) {
        if (this.sidesOpen != sidesOpen) {
            this.sidesOpen = sidesOpen;
            if (!worldObj.isRemote) {
                sendBlockstateToClients();
            } else {
                worldObj.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    public void setEcoMode(boolean newEco) {
        if (newEco != ecoMode) {
            ecoMode = newEco;
            ecoCounter = Config.ecoTimeout;
            if (!worldObj.isRemote) {
                sendBlockstateToClients();
            } else {
                worldObj.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    private void sendBlockstateToClients() {
        worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
    }

    private boolean redstoneModeAllowsRun() {
        switch (redstoneBehaviour) {
            case ALWAYS:
                return true;
            case LOW:
                return !getWorld().isBlockPowered(getPos());
            case HIGH:
                return getWorld().isBlockPowered(getPos());
            case PULSE:
                return true;  // special case; see update() method
            case NEVER:
                return false;
            default:
                return false;
        }
    }

    /**
     * Compile installed modules & upgrades etc. into internal data for faster execution
     */
    private void compile() {
        // modules
        if ((recompileNeeded & COMPILE_MODULES) != 0) {
            ModularRouters.logger.debug("recompiling modules for item router @ " + getPos());
            byte newSidesOpen = 0;
            for (CompiledModule cm : compiledModules) {
                cm.cleanup(this);
            }
            compiledModules.clear();
            for (int i = 0; i < N_MODULE_SLOTS; i++) {
                ItemStack stack = modulesHandler.getStackInSlot(i);
                Module m = ItemModule.getModule(stack);
                if (m != null) {
                    CompiledModule cms = m.compile(this, stack);
                    compiledModules.add(cms);
                    cms.onCompiled(this);
                    newSidesOpen |= cms.getDirection().getMask();
                }
            }
            setSidesOpen(newSidesOpen);
        }

        if ((recompileNeeded & COMPILE_UPGRADES) != 0) {
            ModularRouters.logger.debug("recompiling upgrades for item router @ " + getPos());
            Arrays.fill(upgradeCount, 0);
            permitted.clear();
            for (int i = 0; i < N_UPGRADE_SLOTS; i++) {
                ItemStack stack = upgradesHandler.getStackInSlot(i);
                Upgrade upgrade = ItemUpgrade.getUpgrade(stack);
                if (upgrade != null) {
                    upgradeCount[stack.getItemDamage()] += stack.stackSize;
                    upgrade.onCompiled(stack, this);
                }
            }

            tickRate = calculateTickRate(getUpgradeCount(ItemUpgrade.UpgradeType.SPEED));
            itemsPerTick = calculateItemsPerTick(getUpgradeCount(ItemUpgrade.UpgradeType.STACK));
        }

        recompileNeeded = 0;
    }

    public void setAllowRedstoneEmission(boolean allow) {
        canEmit = allow;
    }

    public int getUpgradeCount(ItemUpgrade.UpgradeType type) {
        return upgradeCount[type.ordinal()];
    }

    public static int calculateTickRate(int nSpeedUpgrades) {
        return Math.max(Config.hardMinTickRate, Config.baseTickRate - Config.ticksPerUpgrade * nSpeedUpgrades);
    }

    public static int calculateItemsPerTick(int nStackUpgrades) {
        return 1 << (Math.min(6, nStackUpgrades));  // 6 upgrades at most => 64 items
    }

    public int getModuleCount() {
        return compiledModules.size();
    }

    public int getSpeedUpgrades() {
        return getUpgradeCount(ItemUpgrade.UpgradeType.SPEED);
    }

    public int getStackUpgrades() {
        return getUpgradeCount(ItemUpgrade.UpgradeType.STACK);
    }

    public int getRangeUpgrades() {
        return getUpgradeCount(ItemUpgrade.UpgradeType.RANGE);
    }

    public void recompileNeeded(int what) {
        recompileNeeded |= what;
    }

    public int getItemsPerTick() {
        return itemsPerTick;
    }

    public EnumFacing getAbsoluteFacing(Module.RelativeDirection direction) {
        IBlockState state = worldObj.getBlockState(pos);
        return direction.toEnumFacing(state.getValue(BlockItemRouter.FACING));
    }

    public ItemStack getBufferItemStack() {
        return bufferHandler.getStackInSlot(0);
    }

    public void playerConfiguringModule(EntityPlayer player, int slotIndex) {
        if (slotIndex >= 0) {
            playerToSlot.put(player.getUniqueID(), slotIndex);
        } else {
            playerToSlot.remove(player.getUniqueID());
        }
    }

    public void clearConfigSlot(EntityPlayer player) {
        playerToSlot.remove(player.getUniqueID());
    }

    public int getConfigSlot(EntityPlayer player) {
        if (playerToSlot.containsKey(player.getUniqueID())) {
            return playerToSlot.get(player.getUniqueID());
        } else {
            return -1;
        }
    }

    public void emitRedstone(Module.RelativeDirection direction, int power, SignalType signalType) {
        if (direction == Module.RelativeDirection.NONE) {
            Arrays.fill(newRedstoneLevels, power);
            Arrays.fill(newSignalType, signalType);
        } else {
            EnumFacing facing = getAbsoluteFacing(direction).getOpposite();
            newRedstoneLevels[facing.ordinal()] = power;
            newSignalType[facing.ordinal()] = signalType;
        }
    }

    public int getRedstoneLevel(EnumFacing facing, boolean strong) {
        if (!canEmit) {
            return 0;
        }
        int i = facing.ordinal();
        if (strong) {
            return signalType[i] == SignalType.STRONG ? redstoneLevels[i] : 0;
        } else {
            return signalType[i] != SignalType.NONE ? redstoneLevels[i] : 0;
        }
    }

    private void handleRedstoneEmission() {
        boolean notifyOwnNeighbours = false;
        EnumSet<EnumFacing> toNotify = EnumSet.noneOf(EnumFacing.class);

        if (!canEmit) {
            // block has stopped being able to emit a signal (all detector modules removed)
            // notify neighbours, and neighbours of neighbours where a strong signal was being emitted
            notifyOwnNeighbours = true;
            for (EnumFacing f : EnumFacing.values()) {
                if (signalType[f.ordinal()] == SignalType.STRONG) {
                    toNotify.add(f.getOpposite());
                }
            }
            Arrays.fill(redstoneLevels, 0);
            Arrays.fill(signalType, SignalType.NONE);
        } else {
            for (EnumFacing facing : EnumFacing.values()) {
                int i = facing.ordinal();
                // if the signal type (strong/weak) has changed, notify neighbours of block in that direction
                // if the signal strength has changed, notify immediate neighbours
                //   - and if signal type is strong, also notify neighbours of neighbour
                if (newSignalType[i] != signalType[i]) {
                    toNotify.add(facing.getOpposite());
                    signalType[i] = newSignalType[i];
                }
                if (newRedstoneLevels[i] != redstoneLevels[i]) {
                    notifyOwnNeighbours = true;
                    if (newSignalType[i] == SignalType.STRONG) {
                        toNotify.add(facing.getOpposite());
                    }
                    redstoneLevels[i] = newRedstoneLevels[i];
                }
            }
        }

        for (EnumFacing f : toNotify) {
            BlockPos pos2 = pos.offset(f);
            worldObj.notifyNeighborsOfStateChange(pos2, worldObj.getBlockState(pos2).getBlock());
        }
        if (notifyOwnNeighbours) {
            worldObj.notifyNeighborsOfStateChange(pos, worldObj.getBlockState(pos).getBlock());
        }
    }

    public void addPermittedIds(Set<UUID> permittedIds) {
        this.permitted.addAll(permittedIds);
    }

    public boolean isPermitted(EntityPlayer player) {
        if (permitted.isEmpty() || permitted.contains(player.getUniqueID())) {
            return true;
        }
        for (EnumHand hand : EnumHand.values()) {
            if (player.getHeldItem(hand) != null && player.getHeldItem(hand).getItem() == ModItems.overrideCard) {
                return true;
            }
        }
        return false;
    }

    public boolean isBufferFull() {
        ItemStack stack = bufferHandler.getStackInSlot(0);
        return stack != null && stack.stackSize >= stack.getMaxStackSize();
    }

    public boolean isBufferEmpty() {
        return bufferHandler.getStackInSlot(0) == null;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Much as I hate to implement IInventory, it's necessary for backwards compatibility...
    // At least it's just a bunch of one-liners

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return bufferHandler.getStackInSlot(index);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        return bufferHandler.extractItem(index, count, false);
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return bufferHandler.extractItem(index, 64, false);
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        bufferHandler.setStackInSlot(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        bufferHandler.setStackInSlot(0, null);
    }

    public boolean getEcoMode() {
        return ecoMode;
    }
}
