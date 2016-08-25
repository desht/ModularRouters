package me.desht.modularrouters.block.tile;

import com.google.common.collect.Sets;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.DetectorModule.SignalType;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.Upgrade;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.network.RouterBlockstateMessage;
import me.desht.modularrouters.proxy.CommonProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityItemRouter extends TileEntity implements ITickable {
    private static final int N_MODULE_SLOTS = 9;
    private static final int N_UPGRADE_SLOTS = 4;

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
    private final ItemStackHandler modulesHandler = new ItemStackHandler(N_MODULE_SLOTS) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack.getItem() instanceof ItemModule ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileEntityItemRouter.this.recompileNeeded(COMPILE_MODULES);
            super.onContentsChanged(slot);
        }
    };
    private final ItemStackHandler upgradesHandler = new ItemStackHandler(N_UPGRADE_SLOTS) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack.getItem() instanceof ItemUpgrade ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileEntityItemRouter.this.recompileNeeded(COMPILE_UPGRADES);
            super.onContentsChanged(slot);
        }
    };
    private final CombinedInvWrapper joined = new CombinedInvWrapper(bufferHandler, modulesHandler, upgradesHandler);

    private final List<CompiledModuleSettings> compiledModuleSettings = new ArrayList<>();
    private byte recompileNeeded = COMPILE_MODULES | COMPILE_UPGRADES;
    private boolean active;
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

    // when a player wants to configure an already-installed module, this tracks the slot
    // number received from the client-side GUI for that player
    private final Map<UUID, Integer> playerToSlot = new HashMap<>();

    private int lastPower;
    private int activeTimer = 0;  // used in PULSE mode to time out the active state

    private final Set<UUID> permitted = Sets.newHashSet(); // permitted user ID's from security upgrade

    // bitmask of which of the 6 sides are currently open
    private byte sidesOpen;

    public TileEntityItemRouter() {
        super();
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
        NBTTagCompound nbtTagCompound = getUpdateTag();
        int metadata = getBlockMetadata();
        return new SPacketUpdateTileEntity(this.pos, metadata, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
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
            if (power > lastPower && counter >= getTickRate()) {
                executeModules();
                counter = 0;
                if (active) {
                    activeTimer = getTickRate();
                }
            }
            // need to turn the state inactive after a short time...
            if (activeTimer > 0) {
                if (--activeTimer == 0) {
                    setActiveState(false);
                }
            }
            lastPower = power;
        } else {
            if (counter >= getTickRate()) {
                executeModules();
                counter = 0;
            }
        }
    }

    private void executeModules() {
        boolean didWork = false;

        if (redstoneModeAllowsRun()) {
            if (prevCanEmit || canEmit) {
                Arrays.fill(newRedstoneLevels, 0);
                Arrays.fill(newSignalType, SignalType.NONE);
            }
            for (CompiledModuleSettings mod : compiledModuleSettings) {
                if (mod != null && mod.execute(this)) {
                    didWork = true;
                    if (mod.termination()) {
                        break;
                    }
                }
            }
            if (prevCanEmit || canEmit) {
                handleRedstoneEmission();
            }
        }
        if (didWork != active) {
            setActiveState(didWork);
        }
        prevCanEmit = canEmit;
    }

    public int getTickRate() {
        return tickRate;
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

    public void setActiveState(boolean newActive) {
        if (active != newActive) {
            active = newActive;
            if (!worldObj.isRemote) {
                sendBlockstateToClients();
            } else {
                worldObj.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    private void sendBlockstateToClients() {
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                worldObj.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64);
        ModularRouters.network.sendToAllAround(new RouterBlockstateMessage(pos, this), point);
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
            canEmit = false;
            compiledModuleSettings.clear();
            for (int i = 0; i < N_MODULE_SLOTS; i++) {
                ItemStack stack = modulesHandler.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ItemModule) {
                    Module m = ItemModule.getModule(stack);
                    if (m == null) {
                        continue; // shouldn't happen but let's be paranoid
                    }
                    if (m instanceof DetectorModule) {
                        canEmit = true;
                    }
                    CompiledModuleSettings cms = m.compile(stack);
                    compiledModuleSettings.add(cms);
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
                if (stack != null && stack.getItemDamage() < upgradeCount.length) {
                    upgradeCount[stack.getItemDamage()] += stack.stackSize;
                    Upgrade u = ItemUpgrade.getUpgrade(stack);
                    if (u != null) {
                        u.onCompiled(stack, this);
                    }
                }
            }

            tickRate = calculateTickRate(getUpgradeCount(ItemUpgrade.UpgradeType.SPEED));
            itemsPerTick = calculateItemsPerTick(getUpgradeCount(ItemUpgrade.UpgradeType.STACK));
        }

        recompileNeeded = 0;
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
        return compiledModuleSettings.size();
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

    public BlockPos getRelativeBlockPos(Module.RelativeDirection direction) {
        return getPos().offset(getAbsoluteFacing(direction));
    }

    public boolean installModule(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack == null || !(stack.getItem() instanceof ItemModule)) {
            return false;
        }

        for (int i = 0; i < modulesHandler.getSlots(); i++) {
            if (modulesHandler.getStackInSlot(i) == null) {
                modulesHandler.setStackInSlot(i, stack);
                player.setHeldItem(hand, null);
                // sound effect?
                recompileNeeded(COMPILE_MODULES);
                return true;
            }
        }
        return false;
    }

    public ItemStack getBufferItemStack() {
        return bufferHandler.getStackInSlot(0);
    }

    public void setBufferItemStack(ItemStack stack) {
        bufferHandler.setStackInSlot(0, stack);
    }

    /**
     * Check if the router processed anything on its last tick
     *
     * @return true if the router processed something
     */
    public boolean isActive() {
        return active;
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

    public boolean isSideOpen(Module.RelativeDirection side) {
        return (sidesOpen & side.getMask()) != 0;
    }

    public void setSidesOpen(byte sidesOpen) {
        if (this.sidesOpen != sidesOpen) {
            this.sidesOpen = sidesOpen;
            if (!worldObj.isRemote) {
                sendBlockstateToClients();
            } else {
                worldObj.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    public byte getSidesOpen() {
        return sidesOpen;
    }
}
