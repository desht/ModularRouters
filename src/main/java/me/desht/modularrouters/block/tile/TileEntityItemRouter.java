package me.desht.modularrouters.block.tile;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockCamo;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.client.util.IHasTranslationKey;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.container.handler.BufferHandler;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModTileEntities;
import me.desht.modularrouters.event.TickEventHandler;
import me.desht.modularrouters.item.module.DetectorModule.SignalType;
import me.desht.modularrouters.item.module.FluidModule1;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.RelativeDirection;
import me.desht.modularrouters.item.upgrade.CamouflageUpgrade;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.SecurityUpgrade;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule1;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.ItemBeamMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.RouterUpgradesSyncMessage;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.fake_player.FakeNetHandlerPlayerServer;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
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
    public static final GameProfile DEFAULT_FAKEPLAYER_PROFILE = new GameProfile(
            UUID.nameUUIDFromBytes(ModularRouters.MODID.getBytes()),
            "[" + ModularRouters.MODNAME + "]"
    );

    private static final int N_MODULE_SLOTS = 9;
    private static final int N_UPGRADE_SLOTS = 5;
    private static final int N_BUFFER_SLOTS = 1;

    public static final int COMPILE_MODULES = 0x01;
    public static final int COMPILE_UPGRADES = 0x02;

    private static final String NBT_ACTIVE = "Active";
    private static final String NBT_ACTIVE_TIMER = "ActiveTimer";
    private static final String NBT_ECO_MODE = "EcoMode";
    private static final String NBT_BUFFER = "Buffer";
    public static final String NBT_MODULES = "Modules";
    public static final String NBT_UPGRADES = "Upgrades";
    private static final String NBT_EXTRA = "Extra";
    public static final String NBT_REDSTONE_MODE = "Redstone";
    private static final String NBT_ENERGY = "EnergyBuffer";
    private static final String NBT_ENERGY_DIR = "EnergyDirection";
    private static final String NBT_ENERGY_UPGRADES = "EnergyUpgrades";

    private int counter = 0;
    private int pulseCounter = 0;

    private RouterRedstoneBehaviour redstoneBehaviour = RouterRedstoneBehaviour.ALWAYS;

    private final BufferHandler bufferHandler = new BufferHandler(this);
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> bufferHandler);

    private final ItemStackHandler modulesHandler = new ModuleHandler();
    private final ItemStackHandler upgradesHandler = new UpgradeHandler();

    private final RouterEnergyBuffer energyStorage = new RouterEnergyBuffer(0);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);
    public final TrackedEnergy trackedEnergy = new TrackedEnergy();
    private EnergyDirection energyDirection = EnergyDirection.FROM_ROUTER;

    private final List<CompiledIndexedModule> compiledModules = new ArrayList<>();
    private byte recompileNeeded = COMPILE_MODULES | COMPILE_UPGRADES;
    private int tickRate = MRConfig.Common.Router.baseTickRate;
    private int itemsPerTick = 1;
    private final Map<ResourceLocation, Integer> upgradeCount = new HashMap<>();

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
    private boolean careAboutItemAttributes;  // whether to bother transferring item attributes to fake player

    public final List<BeamData> beams = new ArrayList<>(); // client-side: beams being rendered
    public final List<BeamData> pendingBeams = new ArrayList<>(); // server-side: beams to be sent to client

    private AxisAlignedBB cachedRenderAABB;

    private RouterFakePlayer fakePlayer;

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

        compound.putInt("x", worldPosition.getX());
        compound.putInt("y", worldPosition.getY());
        compound.putInt("z", worldPosition.getZ());

        if (camouflage != null) {
            compound.put(CamouflageUpgrade.NBT_STATE_NAME, NBTUtil.writeBlockState(camouflage));
        }

        // energy upgrade count sync'd so clientside TE knows if neighbouring cables should be able to connect
        int nEnergy = getUpgradeCount(ModItems.ENERGY_UPGRADE.get());
        if (nEnergy > 0) {
            compound.putInt(NBT_ENERGY_UPGRADES, nEnergy);
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
        return new SUpdateTileEntityPacket(this.worldPosition, -1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        processClientSync(pkt.getTag());
    }

    private void processClientSync(CompoundNBT compound) {
        // called client-side on receipt of NBT

        if (compound.contains(CamouflageUpgrade.NBT_STATE_NAME)) {
            setCamouflage(NBTUtil.readBlockState(compound.getCompound(CamouflageUpgrade.NBT_STATE_NAME)));
        } else {
            setCamouflage(null);
        }

        energyStorage.updateForEnergyUpgrades(compound.getInt(NBT_ENERGY_UPGRADES));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventoryCap.cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return bufferHandler.getFluidCapability().cast();
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
            return bufferHandler.getFluidItemCapability().cast();
        } else if (cap == CapabilityEnergy.ENERGY) {
            return energyStorage.getTransferRate() > 0 ? energyCap.cast() : bufferHandler.getEnergyCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        inventoryCap.invalidate();
        bufferHandler.invalidateCaps();
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);

        bufferHandler.deserializeNBT(nbt.getCompound(NBT_BUFFER));
        modulesHandler.deserializeNBT(nbt.getCompound(NBT_MODULES));
        upgradesHandler.deserializeNBT(nbt.getCompound(NBT_UPGRADES));
        energyStorage.deserializeNBT(nbt.getCompound(NBT_ENERGY));
        energyDirection = EnergyDirection.forValue(nbt.getString(NBT_ENERGY_DIR));
        redstoneBehaviour = RouterRedstoneBehaviour.forValue(nbt.getString(NBT_REDSTONE_MODE));
        active = nbt.getBoolean(NBT_ACTIVE);
        activeTimer = nbt.getInt(NBT_ACTIVE_TIMER);
        ecoMode = nbt.getBoolean(NBT_ECO_MODE);

        CompoundNBT ext = nbt.getCompound(NBT_EXTRA);
        CompoundNBT ext1 = getExtData();
        for (String key : ext.getAllKeys()) {
            //noinspection ConstantConditions
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
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);

        nbt.put(NBT_BUFFER, bufferHandler.serializeNBT());
        if (hasItems(modulesHandler)) nbt.put(NBT_MODULES, modulesHandler.serializeNBT());
        if (hasItems(upgradesHandler)) nbt.put(NBT_UPGRADES, upgradesHandler.serializeNBT());
        if (redstoneBehaviour != RouterRedstoneBehaviour.ALWAYS) nbt.putString(NBT_REDSTONE_MODE, redstoneBehaviour.name());
        if (energyStorage.getCapacity() > 0) nbt.put(NBT_ENERGY, energyStorage.serializeNBT());
        if (energyDirection != EnergyDirection.FROM_ROUTER) nbt.putString(NBT_ENERGY_DIR, energyDirection.name());
        nbt.putBoolean(NBT_ACTIVE, active);
        nbt.putInt(NBT_ACTIVE_TIMER, activeTimer);
        nbt.putBoolean(NBT_ECO_MODE, ecoMode);

        CompoundNBT ext = new CompoundNBT();
        CompoundNBT ext1 = getExtData();
        for (String key : ext1.getAllKeys()) {
            //noinspection ConstantConditions
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
        if (getLevel().isClientSide) {
            for (Iterator<BeamData> iterator = beams.iterator(); iterator.hasNext(); ) {
                BeamData beam = iterator.next();
                beam.tick();
                if (beam.isExpired()) {
                    iterator.remove();
                    cachedRenderAABB = null;
                }
            }
        } else {
            if (recompileNeeded != 0) {
                compile();
            }
            counter++;
            pulseCounter++;

            if (fakePlayer != null) {
                fakePlayer.tick();
                fakePlayer.getCooldowns().tick();
            }

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

            maybeDoEnergyTransfer();
        }
    }

    private void maybeDoEnergyTransfer() {
        if (getEnergyCapacity() > 0 && !getBufferItemStack().isEmpty() && redstoneBehaviour.shouldRun(getRedstonePower() > 0, false)) {
            switch (energyDirection) {
                case FROM_ROUTER:
                    bufferHandler.getEnergyCapability().ifPresent(energyHandler -> {
                        int toExtract = getEnergyStorage().extractEnergy(getEnergyXferRate(), true);
                        int received = energyHandler.receiveEnergy(toExtract, false);
                        getEnergyStorage().extractEnergy(received, false);
                    });
                    break;
                case TO_ROUTER:
                    bufferHandler.getEnergyCapability().ifPresent(energyHandler -> {
                        int toExtract = energyHandler.extractEnergy(getEnergyXferRate(), true);
                        int received = energyStorage.receiveEnergy(toExtract, false);
                        energyHandler.extractEnergy(received, false);
                    });
                    break;
            }
        }
    }

    public RouterFakePlayer getFakePlayer() {
        if (!(getLevel() instanceof ServerWorld)) return null;

        if (fakePlayer == null) {
            fakePlayer = new RouterFakePlayer(this, (ServerWorld) getLevel(), getOwner());
            fakePlayer.connection = new FakeNetHandlerPlayerServer(level.getServer(), fakePlayer);
            fakePlayer.level = level;
            fakePlayer.inventory.selected = 0;  // held item always in slot 0
            fakePlayer.setPosRaw(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        return fakePlayer;
    }

    private GameProfile getOwner() {
        for (int i = 0; i < getUpgrades().getSlots(); i++) {
            ItemStack stack = getUpgrades().getStackInSlot(i);
            if (stack.getItem() instanceof SecurityUpgrade) {
                String name = ((SecurityUpgrade) stack.getItem()).getOwnerName(stack);
                UUID id = ((SecurityUpgrade) stack.getItem()).getOwnerID(stack);
                if (id != null || name != null) {
                    return new GameProfile(id, name);
                }
            }
        }
        return DEFAULT_FAKEPLAYER_PROFILE;
    }

    private void executeModules(boolean pulsed) {
        executing = true;

        boolean newActive = false;

        boolean powered = pulsed || getRedstonePower() > 0;

        if (prevCanEmit || canEmit) {
            Arrays.fill(newRedstoneLevels, 0);
            Arrays.fill(newSignalType, SignalType.NONE);
        }
        if (redstoneBehaviour.shouldRun(powered, pulsed)) {
            for (CompiledIndexedModule cim : compiledModules) {
                CompiledModule cm = cim.compiledModule;
                if (cm != null && cm.hasTarget() && cm.getEnergyCost() <= getEnergyStorage().getEnergyStored() && cm.shouldRun(powered, pulsed))
                    if (cm.execute(this)) {
                        cm.getFilter().cycleRoundRobin().ifPresent(counter -> {
                            ItemStack moduleStack = modulesHandler.getStackInSlot(cim.index);
                            ModuleHelper.setRoundRobinCounter(moduleStack, counter);
                        });
                        getEnergyStorage().extractEnergy(cm.getEnergyCost(), false);
                        newActive = true;
                        if (cm.termination() == ItemModule.Termination.RAN) {
                            break;
                        }
                    } else if (cm.termination() == ItemModule.Termination.NOT_RAN) {
                        break;
                    }
            }
            if (!pendingBeams.isEmpty()) {
                PacketHandler.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> getLevel().getChunkAt(getBlockPos())),
                        new ItemBeamMessage(this, pendingBeams));
                pendingBeams.clear();
            }
        }
        if (prevCanEmit || canEmit) {
            handleRedstoneEmission();
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
        if (this.redstoneBehaviour != redstoneBehaviour) {
            this.redstoneBehaviour = redstoneBehaviour;
            if (redstoneBehaviour == RouterRedstoneBehaviour.PULSE) {
                lastPower = getRedstonePower();
            }
            calculateIncomingRedstonePower(worldPosition);
            handleSync(false);
        }
    }

    private void setActive(boolean newActive) {
        if (active != newActive) {
            active = newActive;
            level.setBlock(getBlockPos(), getBlockState().setValue(BlockItemRouter.ACTIVE,
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
        if (!getLevel().isClientSide) {
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.DEFAULT);
        } else if (getLevel().isClientSide && renderUpdate) {
            requestModelDataUpdate();
            getLevel().setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(BlockCamo.CAMOUFLAGE_STATE, camouflage)
                .build();
    }

    public boolean caresAboutItemAttributes() {
        return careAboutItemAttributes;
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
        getLevel().updateNeighborsAt(worldPosition, state.getBlock());
        setChanged();
        recompileNeeded = 0;
    }

    private void compileModules() {
        if ((recompileNeeded & COMPILE_MODULES) != 0) {
            setHasPulsedModules(false);
            byte newSidesOpen = 0;
            for (CompiledIndexedModule cim : compiledModules) {
                cim.compiledModule.cleanup(this);
            }
            compiledModules.clear();
            careAboutItemAttributes = false;
            for (int i = 0; i < N_MODULE_SLOTS; i++) {
                ItemStack stack = modulesHandler.getStackInSlot(i);
                if (stack.getItem() instanceof ItemModule) {
                    CompiledModule cms = ((ItemModule) stack.getItem()).compile(this, stack);
                    compiledModules.add(new CompiledIndexedModule(cms, i));
                    cms.onCompiled(this);
                    newSidesOpen |= cms.getDirection().getMask();
                    if (cms.careAboutItemAttributes()) careAboutItemAttributes = true;
                }
            }
            setSidesOpen(newSidesOpen);
        }
    }

    private void compileUpgrades() {
        // if called client-side, always recompile (it's due to an upgrade sync)
        if (level.isClientSide || (recompileNeeded & COMPILE_UPGRADES) != 0) {
            int prevMufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
            upgradeCount.clear();
            permitted.clear();
            setCamouflage(null);
            tunedSyncValue = -1;
            for (int i = 0; i < N_UPGRADE_SLOTS; i++) {
                ItemStack stack = upgradesHandler.getStackInSlot(i);
                if (stack.getItem() instanceof ItemUpgrade) {
                    upgradeCount.put(stack.getItem().getRegistryName(), getUpgradeCount(stack.getItem()) + stack.getCount());
                    ((ItemUpgrade) stack.getItem()).onCompiled(stack, this);
                }
            }

            itemsPerTick = 1 << (Math.min(6, getUpgradeCount(ModItems.STACK_UPGRADE.get())));
            tickRate = Math.max(MRConfig.Common.Router.hardMinTickRate,
                    MRConfig.Common.Router.baseTickRate - MRConfig.Common.Router.ticksPerUpgrade * getUpgradeCount(ModItems.SPEED_UPGRADE.get()));
            fluidTransferRate = Math.min(MRConfig.Common.Router.fluidMaxTransferRate,
                    MRConfig.Common.Router.fluidBaseTransferRate + getUpgradeCount(ModItems.FLUID_UPGRADE.get()) * MRConfig.Common.Router.mBperFluidUpgade);

            energyStorage.updateForEnergyUpgrades(getUpgradeCount(ModItems.ENERGY_UPGRADE.get()));
            if (!level.isClientSide) {
                fakePlayer = null; // in case security upgrades change
                int mufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
                if (prevMufflers != mufflers) {
                    level.setBlock(worldPosition, getBlockState().setValue(BlockItemRouter.ACTIVE, active && mufflers < 3), Constants.BlockFlags.BLOCK_UPDATE);
                }
                notifyWatchingPlayers();
            }
        }
    }

    private void notifyWatchingPlayers() {
        for (PlayerEntity player : level.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB(getBlockPos()).inflate(5))) {
            if (player.containerMenu instanceof ContainerItemRouter) {
                if (((ContainerItemRouter) player.containerMenu).getRouter() == this) {
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
        getLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockItemRouter.CAN_EMIT, canEmit));
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
        return direction.toAbsolute(getBlockState().getValue(BlockItemRouter.FACING));
    }

    public ItemStack getBufferItemStack() {
        return bufferHandler.getStackInSlot(0);
    }

    public void checkForRedstonePulse() {
        redstonePower = calculateIncomingRedstonePower(worldPosition);
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
        int i = facing.get3DDataValue();
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
            BlockPos pos2 = worldPosition.relative(f);
            getLevel().updateNeighborsAt(pos2, getLevel().getBlockState(pos2).getBlock());
        }
        if (notifyOwnNeighbours) {
            getLevel().updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public void addPermittedIds(Set<UUID> permittedIds) {
        this.permitted.addAll(permittedIds);
    }

    public boolean isPermitted(PlayerEntity player) {
        if (permitted.isEmpty() || permitted.contains(player.getUUID())) {
            return true;
        }
        for (Hand hand : Hand.values()) {
            if (player.getItemInHand(hand).getItem() == ModItems.OVERRIDE_CARD.get()) {
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
            redstonePower = calculateIncomingRedstonePower(worldPosition);
        }
        return redstonePower;
    }

    private int calculateIncomingRedstonePower(BlockPos pos) {
        // like World#isBlockIndirectlyGettingPowered() but will ignore redstone from any sides
        // currently being extruded on
        int power = 0;
        for (Direction facing : Direction.values()) {
            if (getRedstoneLevel(facing, false) > 0) {
                // ignore signal on any side we ourselves are emitting on
                continue;
            }
            if (getExtData().getInt(CompiledExtruderModule1.NBT_EXTRUDER_DIST + facing) > 0) {
                // ignore signal from any side we're extruding on (don't let placed redstone emitters lock up the router)
                continue;
            }
            int p = getLevel().getSignal(pos.relative(facing), facing);
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
        TileEntity te = world.getBlockEntity(routerPos);
        return te instanceof TileEntityItemRouter ? Optional.of((TileEntityItemRouter) te) : Optional.empty();
    }

    public void playSound(PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0) {
            getLevel().playSound(player, pos, sound, category, volume, pitch);
        }
    }

    public void notifyModules() {
        for (CompiledIndexedModule cim : compiledModules) {
            cim.compiledModule.onNeighbourChange(this);
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
        return new ContainerItemRouter(windowId, playerInventory, this.getBlockPos());
    }

    public GlobalPos getGlobalPos() {
        return MiscUtil.makeGlobalPos(level, worldPosition);
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

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (cachedRenderAABB == null) {
            cachedRenderAABB = super.getRenderBoundingBox();
            beams.forEach(beam -> cachedRenderAABB = cachedRenderAABB.minmax(beam.getAABB(getBlockPos())));
        }
        return cachedRenderAABB;
    }

    public void addItemBeam(BeamData beamData) {
        if (getLevel().isClientSide) {
            beams.add(beamData);
            cachedRenderAABB = null;
        } else {
            pendingBeams.add(beamData);
        }
    }

    public int getEnergyCapacity() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getEnergyXferRate() {
        return energyStorage.getTransferRate();
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void setEnergyDirection(EnergyDirection energyDirection) {
        this.energyDirection = energyDirection;
    }

    public EnergyDirection getEnergyDirection() {
        return energyDirection;
    }

    public enum EnergyDirection implements IHasTranslationKey {
        FROM_ROUTER("from_router"),
        TO_ROUTER("to_router"),
        NONE("none");

        private final String text;

        EnergyDirection(String text) {
            this.text = text;
        }

        public static EnergyDirection forValue(String string) {
            try {
                return EnergyDirection.valueOf(string);
            } catch (IllegalArgumentException e) {
                return FROM_ROUTER;
            }
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.tooltip.energy." + text;
        }
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

    private static class CompiledIndexedModule {
        final CompiledModule compiledModule;
        final int index;

        private CompiledIndexedModule(CompiledModule compiledModule, int index) {
            this.compiledModule = compiledModule;
            this.index = index;
        }
    }

    class RouterEnergyBuffer extends EnergyStorage implements INBTSerializable<CompoundNBT> {
        private int excess;  // "hidden" energy due to energy upgrades being removed

        public RouterEnergyBuffer(int capacity) {
            super(capacity);
            excess = 0;
        }

        @Override
        public boolean canExtract() {
            return super.canExtract() && getRedstoneBehaviour().shouldRun(getRedstonePower() > 0, false);
        }

        @Override
        public boolean canReceive() {
            return super.canReceive() && getRedstoneBehaviour().shouldRun(getRedstonePower() > 0, false);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int n = super.receiveEnergy(maxReceive, simulate);
            if (n != 0 && !simulate) setChanged();
            return n;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int n = super.extractEnergy(maxExtract, simulate);
            if (n != 0 && !simulate) setChanged();
            return n;
        }

        void updateForEnergyUpgrades(int nEnergyUpgrades) {
            int oldCapacity = capacity;
            capacity = MRConfig.Common.Router.fePerEnergyUpgrade * nEnergyUpgrades;
            if (energy > capacity) {
                // now not enough capacity - stow the excess energy
                excess += energy - capacity;
                energy = capacity;
            } else {
                // more capacity than energy - move what we can from excess to main storage
                int available = capacity - energy;
                int toMove = Math.min(available, excess);
                excess -= toMove;
                energy += toMove;
            }
            maxExtract = maxReceive = MRConfig.Common.Router.feXferPerEnergyUpgrade * nEnergyUpgrades;
            if (oldCapacity == 0 && capacity != 0 || oldCapacity != 0 && capacity == 0) {
                // in case any pipes/cables need to connect/disconnect
                getLevel().updateNeighborsAt(getBlockPos(), ModBlocks.ITEM_ROUTER.get());
            }
        }

        public int getTransferRate() {
            return maxExtract;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("Energy", energy);
            tag.putInt("Capacity", capacity);
            tag.putInt("Excess", excess);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            energy = nbt.getInt("Energy");
            capacity = nbt.getInt("Capacity");
            excess = nbt.getInt("Excess");
        }

        public int getCapacity() {
            return capacity;
        }

        void setEnergyStored(int energyStored) {
            // only called client side for gui sync purposes
            this.energy = Math.min(energyStored, capacity);
        }
    }

    public class TrackedEnergy implements IIntArray {
        @Override
        public int get(int idx) {
            int res = 0;
            if (idx == 0) {
                res = energyStorage.getEnergyStored() & 0x0000FFFF;
            } else if (idx == 1) {
                res = (energyStorage.getEnergyStored() & 0xFFFF0000) >> 16;
            }
            return res;
        }

        @Override
        public void set(int idx, int val) {
            if (val < 0) val += 65536;  // due to int->short conversion silliness in SWindowPropertyPacket
            if (idx == 0) {
                energyStorage.setEnergyStored(energyStorage.getEnergyStored() & 0xFFFF0000 | val);
            } else if (idx == 1) {
                energyStorage.setEnergyStored(energyStorage.getEnergyStored() & 0x0000FFFF | val << 16);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
