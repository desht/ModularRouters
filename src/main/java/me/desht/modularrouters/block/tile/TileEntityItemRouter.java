package me.desht.modularrouters.block.tile;

import com.google.common.collect.Sets;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockCamo;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.container.handler.BufferHandler;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModTileEntities;
import me.desht.modularrouters.event.TickEventHandler;
import me.desht.modularrouters.item.module.DetectorModule.SignalType;
import me.desht.modularrouters.item.module.FluidModule1;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.RelativeDirection;
import me.desht.modularrouters.item.upgrade.CamouflageUpgrade;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule1;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.RouterUpgradesSyncMessage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class TileEntityItemRouter extends TileEntity implements ITickableTileEntity, ICamouflageable, INamedContainerProvider {
    private static final int N_MODULE_SLOTS = 9;
    private static final int N_UPGRADE_SLOTS = 5;
    private static final int N_BUFFER_SLOTS = 1;

    public static final int COMPILE_MODULES = 0x01;
    public static final int COMPILE_UPGRADES = 0x02;

    private static final String NBT_ACTIVE = "Active";
    private static final String NBT_ACTIVE_TIMER = "ActiveTimer";
    private static final String NBT_ECO_MODE = "EcoMode";
    private static final String NBT_SIDES = "Sides";
    private static final String NBT_BUFFER = "Buffer";
    public static final String NBT_MODULES = "Modules";
    public static final String NBT_UPGRADES = "Upgrades";
    private static final String NBT_EXTRA = "Extra";
    public static final String NBT_REDSTONE_MODE = "Redstone";
    private static final String NBT_MUFFLERS = "Mufflers";

    private int counter = 0;
    private int pulseCounter = 0;

    private RouterRedstoneBehaviour redstoneBehaviour = RouterRedstoneBehaviour.ALWAYS;

    private final BufferHandler bufferHandler = new BufferHandler(this);
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> bufferHandler);

    private final ItemStackHandler modulesHandler = new ModuleHandler();
    private final ItemStackHandler upgradesHandler = new UpgradeHandler();

    private final List<CompiledModule> compiledModules = new ArrayList<>();
    private byte recompileNeeded = COMPILE_MODULES | COMPILE_UPGRADES;
    private int tickRate = MRConfig.Common.Router.baseTickRate;
    private int itemsPerTick = 1;
    private final Map<ResourceLocation, Integer> upgradeCount = new HashMap<>();
    private int totalUpgradeCount;

    private int fluidTransferRate;  // mB/t
    private int fluidTransferRemainingIn = 0;
    private int fluidTransferRemainingOut = 0;

    // for tracking redstone emission levels for the detector module
    private final int SIDES = Direction.values().length;
    private final int[] redstoneLevels = new int[SIDES];
    private final int[] newRedstoneLevels = new int[SIDES];
    private final SignalType[] signalType = new SignalType[SIDES];
    private final SignalType[] newSignalType = new SignalType[SIDES];
    private boolean canEmit, prevCanEmit; // used if 1 or more detector modules are installed
    private int redstonePower = -1;  // current redstone power (updated via onNeighborChange())
    private int lastPower;           // tracks previous redstone power level for pulse mode
    private boolean active;          // tracks active state of router
    private int activeTimer = 0;     // used in PULSE mode to time out the active state
    private final Set<UUID> permitted = Sets.newHashSet(); // permitted user ID's from security upgrade
    private byte sidesOpen;          // bitmask of which of the 6 sides are currently open
    private boolean ecoMode = false;  // track eco-mode
    private int ecoCounter = MRConfig.Common.Router.ecoTimeout;
    private boolean hasPulsedModules = false;
    private CompoundNBT extData;  // extra (persisted) data which various modules can set & read
    private BlockState camouflage = null;  // block to masquerade as, set by Camo Upgrade
    private int tunedSyncValue = -1; // for synchronisation tuning, set by Sync Upgrade
    private boolean executing;       // are we currently executing modules?

    public TileEntityItemRouter() {
        super(ModTileEntities.ITEM_ROUTER.get());
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
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = new CompoundNBT();

        compound.putInt("x", pos.getX());
        compound.putInt("y", pos.getY());
        compound.putInt("z", pos.getZ());

        if (camouflage != null) {
            compound.put(CamouflageUpgrade.NBT_STATE_NAME, NBTUtil.writeBlockState(camouflage));
        }
        return compound;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        processClientSync(tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, -1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        processClientSync(pkt.getNbtCompound());
    }

    private void processClientSync(CompoundNBT compound) {
        // called client-side

        if (compound.contains(CamouflageUpgrade.NBT_STATE_NAME)) {
            setCamouflage(NBTUtil.readBlockState(compound.getCompound(CamouflageUpgrade.NBT_STATE_NAME)));
        } else {
            setCamouflage(null);
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, inventoryCap);
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, bufferHandler.getFluidCapability());
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.orEmpty(cap, bufferHandler.getFluidItemCapability());
        } else if (cap == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(cap, bufferHandler.getEnergyCapability());
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        bufferHandler.deserializeNBT(nbt.getCompound(NBT_BUFFER));
        modulesHandler.deserializeNBT(nbt.getCompound(NBT_MODULES));
        upgradesHandler.deserializeNBT(nbt.getCompound(NBT_UPGRADES));
        try {
            redstoneBehaviour = RouterRedstoneBehaviour.valueOf(nbt.getString(NBT_REDSTONE_MODE));
        } catch (IllegalArgumentException e) {
            // shouldn't ever happen...
            redstoneBehaviour = RouterRedstoneBehaviour.ALWAYS;
        }
        active = nbt.getBoolean(NBT_ACTIVE);
        activeTimer = nbt.getInt(NBT_ACTIVE_TIMER);
        ecoMode = nbt.getBoolean(NBT_ECO_MODE);

        CompoundNBT ext = nbt.getCompound(NBT_EXTRA);
        CompoundNBT ext1 = getExtData();
        for (String key : ext.keySet()) {
            ext1.put(key, ext.get(key));
        }

        // When restoring, give the counter a random initial value to avoid all saved routers
        // having the same counter and firing simultaneously, which could conceivably cause lag
        // spikes if there are many routers in the world.
        // The -1 value indicates that a random value should be picked at the next compile.
        counter = -1;
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt = super.write(nbt);
        nbt.put(NBT_BUFFER, bufferHandler.serializeNBT());
        if (hasItems(modulesHandler)) nbt.put(NBT_MODULES, modulesHandler.serializeNBT());
        if (hasItems(upgradesHandler)) nbt.put(NBT_UPGRADES, upgradesHandler.serializeNBT());
        if (redstoneBehaviour != RouterRedstoneBehaviour.ALWAYS) nbt.putString(NBT_REDSTONE_MODE, redstoneBehaviour.name());
        nbt.putBoolean(NBT_ACTIVE, active);
        nbt.putInt(NBT_ACTIVE_TIMER, activeTimer);
        nbt.putBoolean(NBT_ECO_MODE, ecoMode);

        CompoundNBT ext = new CompoundNBT();
        CompoundNBT ext1 = getExtData();
        for (String key : ext1.keySet()) {
            ext.put(key, ext1.get(key));
        }
        nbt.put(NBT_EXTRA, ext);

        return nbt;
    }

    private boolean hasItems(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (getWorld().isRemote) {
            return;
        }

        if (recompileNeeded != 0) {
            compile();
        }
        counter++;
        pulseCounter++;

        if (getRedstoneBehaviour() == RouterRedstoneBehaviour.PULSE) {
            // pulse checking is done by checkRedstonePulse() - called from BlockItemRouter#neighborChanged()
            // however, we do need to turn the state inactive after a short time if we were set active by a pulse
            if (activeTimer > 0 && --activeTimer == 0) {
                setActive(false);
            }
        } else {
            if (counter >= getTickRate()) {
                allocateFluidTransfer(counter);
                executeModules(false);
                counter = 0;
            }
        }

        if (ecoMode) {
            if (active) {
                ecoCounter = MRConfig.Common.Router.ecoTimeout;
            } else if (ecoCounter > 0) {
                ecoCounter--;
            }
        }
    }

    private void executeModules(boolean pulsed) {
        executing = true;

        boolean newActive = false;

        boolean powered = pulsed || getRedstonePower() > 0;

        if (redstoneBehaviour.shouldRun(powered, pulsed)) {
            if (prevCanEmit || canEmit) {
                Arrays.fill(newRedstoneLevels, 0);
                Arrays.fill(newSignalType, SignalType.NONE);
            }
            for (CompiledModule cm : compiledModules) {
                if (cm != null && cm.hasTarget() && cm.shouldRun(powered, pulsed) && cm.execute(this)) {
                    newActive = true;
                    if (cm.termination()) {
                        break;
                    }
                }
            }
            if (prevCanEmit || canEmit) {
                handleRedstoneEmission();
            }
        }
        setActive(newActive);
        prevCanEmit = canEmit;
        executing = false;
    }

    public int getTickRate() {
        return ecoMode && ecoCounter == 0 ? MRConfig.Common.Router.lowPowerTickRate : tickRate;
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public void setRedstoneBehaviour(RouterRedstoneBehaviour redstoneBehaviour) {
        this.redstoneBehaviour = redstoneBehaviour;
        if (redstoneBehaviour == RouterRedstoneBehaviour.PULSE) {
            lastPower = getRedstonePower();
        }
        handleSync(false);
    }

    private void setActive(boolean newActive) {
        if (active != newActive) {
            active = newActive;
            world.setBlockState(getPos(), getBlockState().with(BlockItemRouter.ACTIVE,
                    newActive && getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 3), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    private void setSidesOpen(byte sidesOpen) {
        if (this.sidesOpen != sidesOpen) {
            this.sidesOpen = sidesOpen;
            handleSync(true);
        }
    }

    public void setEcoMode(boolean newEco) {
        if (newEco != ecoMode) {
            ecoMode = newEco;
            ecoCounter = MRConfig.Common.Router.ecoTimeout;
        }
    }

    @Override
    public BlockState getCamouflage() {
        return camouflage;
    }

    @Override
    public void setCamouflage(BlockState newCamouflage) {
        if (newCamouflage != camouflage) {
            this.camouflage = newCamouflage;
            handleSync(true);
        }
    }

    private void handleSync(boolean renderUpdate) {
        // some tile entity field changed that the client needs to know about
        // if on server, sync TE data to client; if on client, possibly mark the TE for re-render
        if (!getWorld().isRemote) {
            getWorld().notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT);
        } else if (getWorld().isRemote && renderUpdate) {
            requestModelDataUpdate();
            getWorld().markBlockRangeForRenderUpdate(pos, Blocks.AIR.getDefaultState(), getBlockState());
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(BlockCamo.CAMOUFLAGE_STATE, camouflage)
                .build();
    }

    /**
     * Compile installed modules & upgrades etc. into internal data for faster execution.  Only called
     * server-side (although compileUpgrades() can be called clientside when upgrades are sync'd)
     */
    private void compile() {
        compileUpgrades();
        compileModules();

        if (tunedSyncValue >= 0) {
            // router has a sync upgrade - init the counter accordingly
            counter = calculateSyncCounter();
        } else if (counter < 0) {
            // we've just restored from NBT - start off with a random counter value
            // to avoid lots of routers all ticking at the same time
            counter = new Random().nextInt(tickRate);
        }

        BlockState state = getBlockState();
        getWorld().notifyNeighborsOfStateChange(pos, state.getBlock());
        markDirty();
        recompileNeeded = 0;
    }

    private void compileModules() {
        if ((recompileNeeded & COMPILE_MODULES) != 0) {
            setHasPulsedModules(false);
            byte newSidesOpen = 0;
            for (CompiledModule cm : compiledModules) {
                cm.cleanup(this);
            }
            compiledModules.clear();
            for (int i = 0; i < N_MODULE_SLOTS; i++) {
                ItemStack stack = modulesHandler.getStackInSlot(i);
                if (stack.getItem() instanceof ItemModule) {
                    CompiledModule cms = ((ItemModule) stack.getItem()).compile(this, stack);
                    compiledModules.add(cms);
                    cms.onCompiled(this);
                    newSidesOpen |= cms.getDirection().getMask();
                }
            }
            setSidesOpen(newSidesOpen);
        }
    }

    private void compileUpgrades() {
        // if called client-side, always recompile (it's due to an upgrade sync)
        if (world.isRemote || (recompileNeeded & COMPILE_UPGRADES) != 0) {
            int prevMufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
            upgradeCount.clear();
            totalUpgradeCount = 0;
            permitted.clear();
            setCamouflage(null);
            tunedSyncValue = -1;
            for (int i = 0; i < N_UPGRADE_SLOTS; i++) {
                ItemStack stack = upgradesHandler.getStackInSlot(i);
                if (stack.getItem() instanceof ItemUpgrade) {
                    upgradeCount.put(stack.getItem().getRegistryName(), getUpgradeCount(stack.getItem()) + stack.getCount());
                    totalUpgradeCount += stack.getCount();
                    ((ItemUpgrade) stack.getItem()).onCompiled(stack, this);
                }
            }

            itemsPerTick = 1 << (Math.min(6, getUpgradeCount(ModItems.STACK_UPGRADE.get())));
            tickRate = Math.max(MRConfig.Common.Router.hardMinTickRate,
                    MRConfig.Common.Router.baseTickRate - MRConfig.Common.Router.ticksPerUpgrade * getUpgradeCount(ModItems.SPEED_UPGRADE.get()));
            fluidTransferRate = Math.min(MRConfig.Common.Router.fluidMaxTransferRate,
                    MRConfig.Common.Router.fluidBaseTransferRate + getUpgradeCount(ModItems.FLUID_UPGRADE.get()) * MRConfig.Common.Router.mBperFluidUpgade);
            if (!world.isRemote) {
                int mufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
                if (prevMufflers != mufflers) {
                    world.setBlockState(pos, getBlockState().with(BlockItemRouter.ACTIVE, active && mufflers < 3), Constants.BlockFlags.BLOCK_UPDATE);
                }
                notifyWatchingPlayers();
            }
        }
    }

    private void notifyWatchingPlayers() {
        for (PlayerEntity player : world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(getPos()).grow(5))) {
            if (player.openContainer instanceof ContainerItemRouter) {
                if (((ContainerItemRouter) player.openContainer).getRouter() == this) {
                    PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new RouterUpgradesSyncMessage(this));
                }
            }
        }
    }

    public void setTunedSyncValue(int newValue) {
        tunedSyncValue = newValue;
    }

    private int calculateSyncCounter() {
        // use our global tick counter and router's tick rate to determine a value
        // for the sync counter that ensures the router always executes at a certain time
        int compileTime = (int) TickEventHandler.TickCounter % tickRate;
        int tuning = tunedSyncValue % tickRate;
        int delta = tuning - compileTime;
        if (delta <= 0) delta += tickRate;

        return tickRate - delta;
    }

    public void setAllowRedstoneEmission(boolean allow) {
        canEmit = allow;
        getWorld().setBlockState(pos, getBlockState().with(BlockItemRouter.CAN_EMIT, canEmit));
    }

    public int getUpgradeCount(Item type) {
        return upgradeCount.getOrDefault(type.getRegistryName(), 0);
    }

    public void recompileNeeded(int what) {
        recompileNeeded |= what;
    }

    public int getItemsPerTick() {
        return itemsPerTick;
    }

    private void allocateFluidTransfer(int ticks) {
        // increment the in/out fluid transfer allowance based on the number of ticks which have passed
        // and the current fluid transfer rate of the router (which depends on the number of fluid upgrades)
        int maxTransfer = MRConfig.Common.Router.baseTickRate * fluidTransferRate;
        fluidTransferRemainingIn = Math.min(fluidTransferRemainingIn + ticks * fluidTransferRate, maxTransfer);
        fluidTransferRemainingOut = Math.min(fluidTransferRemainingOut + ticks * fluidTransferRate, maxTransfer);
    }

    public int getFluidTransferRate() {
        return fluidTransferRate;
    }

    public int getCurrentFluidTransferAllowance(FluidModule1.FluidDirection dir) {
        return dir == FluidModule1.FluidDirection.IN ? fluidTransferRemainingIn : fluidTransferRemainingOut;
    }

    public void transferredFluid(int amount, FluidModule1.FluidDirection dir) {
        switch (dir) {
            case IN:
                if (fluidTransferRemainingIn < amount) ModularRouters.LOGGER.warn("fluid transfer: " + fluidTransferRemainingIn + " < " + amount);
                fluidTransferRemainingIn = Math.max(0, fluidTransferRemainingIn - amount);
                break;
            case OUT:
                if (fluidTransferRemainingOut < amount) ModularRouters.LOGGER.warn("fluid transfer: " + fluidTransferRemainingOut + " < " + amount);
                fluidTransferRemainingOut = Math.max(0, fluidTransferRemainingOut - amount);
                break;
            default:
                break;
        }
    }

    public Direction getAbsoluteFacing(RelativeDirection direction) {
        return direction.toAbsolute(getBlockState().get(BlockItemRouter.FACING));
    }

    public ItemStack getBufferItemStack() {
        return bufferHandler.getStackInSlot(0);
    }

    public void checkForRedstonePulse() {
        redstonePower = calculateIncomingRedstonePower(pos);
        if (executing) {
            return;  // avoid recursion from executing module triggering more block updates
        }
        if (redstoneBehaviour == RouterRedstoneBehaviour.PULSE
                || hasPulsedModules && redstoneBehaviour == RouterRedstoneBehaviour.ALWAYS) {
            if (redstonePower > lastPower && pulseCounter >= tickRate) {
                allocateFluidTransfer(Math.min(pulseCounter, MRConfig.Common.Router.baseTickRate));
                executeModules(true);
                pulseCounter = 0;
                if (active) {
                    activeTimer = tickRate;
                }
            }
            lastPower = redstonePower;
        }
    }

    public void emitRedstone(RelativeDirection direction, int power, SignalType signalType) {
        if (direction == RelativeDirection.NONE) {
            Arrays.fill(newRedstoneLevels, power);
            Arrays.fill(newSignalType, signalType);
        } else {
            Direction facing = getAbsoluteFacing(direction).getOpposite();
            newRedstoneLevels[facing.ordinal()] = power;
            newSignalType[facing.ordinal()] = signalType;
        }
    }

    public int getRedstoneLevel(Direction facing, boolean strong) {
        if (!canEmit) {
            // -1 means the block shouldn't have any special redstone handling
            return -1;
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
        EnumSet<Direction> toNotify = EnumSet.noneOf(Direction.class);

        if (!canEmit) {
            // block has stopped being able to emit a signal (all detector modules removed)
            // notify neighbours, and neighbours of neighbours where a strong signal was being emitted
            notifyOwnNeighbours = true;
            for (Direction f : Direction.values()) {
                if (signalType[f.ordinal()] == SignalType.STRONG) {
                    toNotify.add(f.getOpposite());
                }
            }
            Arrays.fill(redstoneLevels, 0);
            Arrays.fill(signalType, SignalType.NONE);
        } else {
            for (Direction facing : Direction.values()) {
                int i = facing.ordinal();
                // if the signal op (strong/weak) has changed, notify neighbours of block in that direction
                // if the signal strength has changed, notify immediate neighbours
                //   - and if signal op is strong, also notify neighbours of neighbour
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

        for (Direction f : toNotify) {
            BlockPos pos2 = pos.offset(f);
            getWorld().notifyNeighborsOfStateChange(pos2, getWorld().getBlockState(pos2).getBlock());
        }
        if (notifyOwnNeighbours) {
            getWorld().notifyNeighborsOfStateChange(pos, getBlockState().getBlock());
        }
    }

    public void addPermittedIds(Set<UUID> permittedIds) {
        this.permitted.addAll(permittedIds);
    }

    public boolean isPermitted(PlayerEntity player) {
        if (permitted.isEmpty() || permitted.contains(player.getUniqueID())) {
            return true;
        }
        for (Hand hand : Hand.values()) {
            if (player.getHeldItem(hand).getItem() == ModItems.OVERRIDE_CARD.get()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBufferFull() {
        ItemStack stack = bufferHandler.getStackInSlot(0);
        return !stack.isEmpty() && stack.getCount() >= stack.getMaxStackSize();
    }

    public boolean isBufferEmpty() {
        return bufferHandler.getStackInSlot(0).isEmpty();
    }

    public ItemStack peekBuffer(int amount) {
        return bufferHandler.extractItem(0, amount, true);
    }

    public ItemStack extractBuffer(int amount) {
        return bufferHandler.extractItem(0, amount, false);
    }

    public ItemStack insertBuffer(ItemStack stack) {
        return bufferHandler.insertItem(0, stack, false);
    }

    public void setBufferItemStack(ItemStack stack) {
        bufferHandler.setStackInSlot(0, stack);
    }

    public boolean getEcoMode() {
        return ecoMode;
    }

    public void setHasPulsedModules(boolean hasPulsedModules) {
        this.hasPulsedModules = hasPulsedModules;
    }

    public int getRedstonePower() {
        if (redstonePower < 0) {
            redstonePower = calculateIncomingRedstonePower(pos);
        }
        return redstonePower;
    }

    private int calculateIncomingRedstonePower(BlockPos pos) {
        // like World#isBlockIndirectlyGettingPowered() but will ignore redstone from any sides
        // currently being extruded on
        int power = 0;
        for (Direction facing : Direction.values()) {
            if (getExtData().getInt(CompiledExtruderModule1.NBT_EXTRUDER_DIST + facing) > 0) {
                // ignore signal from any side we're extruding on (don't let placed redstone emitters lock up the router)
                continue;
            }
            int p = getWorld().getRedstonePower(pos.offset(facing), facing);
            if (p >= 15) {
                return p;
            } else if (p > power) {
                power = p;
            }
        }
        return power;
    }

    public CompoundNBT getExtData() {
        if (extData == null) {
            extData = new CompoundNBT();
        }
        return extData;
    }

    public static Optional<TileEntityItemRouter> getRouterAt(IBlockReader world, BlockPos routerPos) {
        TileEntity te = world.getTileEntity(routerPos);
        return te instanceof TileEntityItemRouter ? Optional.of((TileEntityItemRouter) te) : Optional.empty();
    }

    public void playSound(PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0) {
            getWorld().playSound(player, pos, sound, category, volume, pitch);
        }
    }

    public void notifyModules() {
        for (CompiledModule cm : compiledModules) {
            cm.onNeighbourChange(this);
        }
    }

    public int getModuleSlotCount() {
        return N_MODULE_SLOTS;
    }

    public int getUpgradeSlotCount() {
        return N_UPGRADE_SLOTS;
    }

    public int getBufferSlotCount() {
        return N_BUFFER_SLOTS;
    }

    public int getModuleCount() {
        return compiledModules.size();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.modularrouters.item_router");
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerItemRouter(windowId, playerInventory, this.getPos());
    }

    public GlobalPos getGlobalPos() {
        return GlobalPos.func_239648_a_(world.func_234923_W_(), pos);
    }

    /**
     * Set the upgrades to the given set.  Used client-side for upgrade sync'ing.
     *
     * @param handler item handler containing new set of upgrades
     */
    public void setUpgradesFrom(IItemHandler handler) {
        if (handler.getSlots() == upgradesHandler.getSlots()) {
            for (int i = 0; i < handler.getSlots(); i++) {
                upgradesHandler.setStackInSlot(i, handler.getStackInSlot(i));
            }
        }
        compileUpgrades();
    }

    abstract class RouterItemHandler extends ItemStackHandler {
        private final Predicate<ItemStack> validator;
        private final int flag;

        private RouterItemHandler(int flag, int size, Predicate<ItemStack> validator) {
            super(size);
            this.validator = validator;
            this.flag = flag;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return super.isItemValid(slot, stack) && validator.test(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            recompileNeeded(flag);
        }
    }

    class ModuleHandler extends RouterItemHandler {
        ModuleHandler() {
            super(TileEntityItemRouter.COMPILE_MODULES, getModuleSlotCount(), s -> s.getItem() instanceof ItemModule);
        }
    }

    class UpgradeHandler extends RouterItemHandler {
        UpgradeHandler() {
            super(TileEntityItemRouter.COMPILE_UPGRADES, getUpgradeSlotCount(), s -> s.getItem() instanceof ItemUpgrade);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            // can't have the same upgrade in more than one slot
            for (int i = 0; i < getSlots(); i++) {
                if (slot != i && stack.getItem() == getStackInSlot(i).getItem()) return false;
            }
            return super.isItemValid(slot, stack);
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            if (!(stack.getItem() instanceof ItemUpgrade)) return 0;
            return ((ItemUpgrade) stack.getItem()).getStackLimit(slot);
        }
    }
}
