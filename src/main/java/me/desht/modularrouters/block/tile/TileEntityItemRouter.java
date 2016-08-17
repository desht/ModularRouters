package me.desht.modularrouters.block.tile;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
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

    private final int[] redstoneLevels = new int[EnumFacing.values().length];
    private final int[] newRedstoneLevels = new int[EnumFacing.values().length];

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
            TileEntityItemRouter.this.recompileNeeded();
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
            TileEntityItemRouter.this.recompileNeeded();
            super.onContentsChanged(slot);
        }
    };
    private final CombinedInvWrapper joined = new CombinedInvWrapper(bufferHandler, modulesHandler, upgradesHandler);

    private final List<CompiledModuleSettings> compiledModuleSettings = new ArrayList<>();
    private boolean recompileNeeded = true;
    private boolean active;
    private int tickRate = Config.baseTickRate;
    private int itemsPerTick = 1;
    private final int[] upgradeCount = new int[ItemUpgrade.UpgradeType.values().length];

    // when player wants to configure an already-installed module, this tracks the slot
    // number received from the client-side GUI
    private final Map<UUID, Integer> playerToSlot = new HashMap<>();
    private int lastPower;
    private int activeTimer = 0;

    public TileEntityItemRouter() {
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
        if (recompileNeeded) {
            ModularRouters.logger.debug("recompiling item router @ " + getPos());
            compile();
            recompileNeeded = false;
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

    private void executeModules() {
        boolean didWork = false;

        if (redstoneModeAllowsRun()) {
            Arrays.fill(newRedstoneLevels, 0);

            for (CompiledModuleSettings mod : compiledModuleSettings) {
                if (mod != null && mod.execute(this)) {
                    didWork = true;
                    if (mod.termination()) {
                        break;
                    }
                }
            }

            if (!Arrays.equals(redstoneLevels, newRedstoneLevels)) {
                System.arraycopy(newRedstoneLevels, 0, redstoneLevels, 0, redstoneLevels.length);
                worldObj.notifyNeighborsOfStateChange(pos, worldObj.getBlockState(pos).getBlock());
            }
        }
        if (didWork != active) {
            setActiveState(didWork);
        }
    }

    private void setActiveState(boolean newActive) {
        active = newActive;
        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().setBlockState(getPos(), state.withProperty(BlockItemRouter.ACTIVE, newActive));
        markDirty();
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
        compiledModuleSettings.clear();
        for (int i = 0; i < N_MODULE_SLOTS; i++) {
            ItemStack stack = modulesHandler.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemModule) {
                compiledModuleSettings.add(ItemModule.getModule(stack).compile(stack));
            }
        }

        for (int i = 0; i < upgradeCount.length; i++) {
            upgradeCount[i] = 0;
        }
        for (int i = 0; i < N_UPGRADE_SLOTS; i++) {
            ItemStack stack = upgradesHandler.getStackInSlot(i);
            if (stack != null && stack.getItemDamage() < upgradeCount.length) {
                upgradeCount[stack.getItemDamage()] += stack.stackSize;
            }
        }

        tickRate = calculateTickRate(getUpgradeCount(ItemUpgrade.UpgradeType.SPEED));
        itemsPerTick = calculateItemsPerTick(getUpgradeCount(ItemUpgrade.UpgradeType.STACK));
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

    public void recompileNeeded() {
        recompileNeeded = true;
    }

    public int getItemsPerTick() {
        return itemsPerTick;
    }

    public EnumFacing getAbsoluteFacing(Module.RelativeDirection direction) {
        IBlockState state = getWorld().getBlockState(getPos());
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
                recompileNeeded = true;
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

    public void emitRedstone(Module.RelativeDirection direction, int power) {
        if (direction == Module.RelativeDirection.NONE) {
            Arrays.fill(newRedstoneLevels, power);
        } else {
            EnumFacing facing = getAbsoluteFacing(direction).getOpposite();
            newRedstoneLevels[facing.ordinal()] = power;
        }
    }

    public int getRedstoneLevel(EnumFacing facing) {
        return redstoneLevels[facing.ordinal()];
    }
}
