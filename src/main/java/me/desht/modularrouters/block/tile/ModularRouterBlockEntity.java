package me.desht.modularrouters.block.tile;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.CamouflageableBlock;
import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.RouterMenu;
import me.desht.modularrouters.container.handler.BufferHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.event.TickEventHandler;
import me.desht.modularrouters.item.module.DetectorModule.SignalType;
import me.desht.modularrouters.item.module.FluidModule1;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.module.ModuleItem.RelativeDirection;
import me.desht.modularrouters.item.upgrade.CamouflageUpgrade;
import me.desht.modularrouters.item.upgrade.SecurityUpgrade;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule1;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.network.messages.ItemBeamMessage;
import me.desht.modularrouters.network.messages.RouterUpgradesSyncMessage;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.TranslatableEnum;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class ModularRouterBlockEntity extends BlockEntity implements ICamouflageable, MenuProvider {
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
    private final ItemStackHandler modulesHandler = new ModuleHandler();
    private final ItemStackHandler upgradesHandler = new UpgradeHandler();

    private final RouterEnergyBuffer energyStorage = new RouterEnergyBuffer(0);

    public final TrackedEnergy trackedEnergy = new TrackedEnergy();
    private EnergyDirection energyDirection = EnergyDirection.FROM_ROUTER;

    private final List<CompiledIndexedModule> compiledModules = new ArrayList<>();
    private byte recompileNeeded = COMPILE_MODULES | COMPILE_UPGRADES;
    private int tickRate = ConfigHolder.common.router.baseTickRate.get();
    private int itemsPerTick = 1;
    private final Map<UpgradeItem, Integer> upgradeCount = new HashMap<>();

    private int fluidTransferRate;  // mB/t
    private int fluidTransferRemainingIn = 0;
    private int fluidTransferRemainingOut = 0;

    // for tracking redstone emission levels for the detector module
    private final int SIDES = MiscUtil.DIRECTIONS.length;
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
    private int ecoCounter = ConfigHolder.common.router.ecoTimeout.get();
    private boolean hasPulsedModules = false;
    private CompoundTag extData;  // extra (persisted) data which various modules can set & read
    private BlockState camouflage = null;  // block to masquerade as, set by Camo Upgrade
    private int tunedSyncValue = -1; // for synchronisation tuning, set by Sync Upgrade
    private boolean executing;       // are we currently executing modules?
    private boolean careAboutItemAttributes;  // whether to bother transferring item attributes to fake player

    public final List<BeamData> beams = new ArrayList<>(); // client-side: beams being rendered
    public final List<BeamData> pendingBeams = new ArrayList<>(); // server-side: beams to be sent to client

    private AABB cachedRenderAABB;

    private RouterFakePlayer fakePlayer;

    public ModularRouterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MODULAR_ROUTER.get(), pos, state);
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

    @Nonnull
    public Level nonNullLevel() {
        return Objects.requireNonNull(level);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return Util.make(new CompoundTag(), tag -> {
            tag.putInt("x", worldPosition.getX());
            tag.putInt("y", worldPosition.getY());
            tag.putInt("z", worldPosition.getZ());

            if (camouflage != null) {
                tag.put(CamouflageUpgrade.NBT_STATE_NAME, NbtUtils.writeBlockState(camouflage));
            }

            // energy upgrade count sync'd so clientside TE knows if neighbouring cables should be able to connect
            int nEnergy = getUpgradeCount(ModItems.ENERGY_UPGRADE.get());
            if (nEnergy > 0) {
                tag.putInt(NBT_ENERGY_UPGRADES, nEnergy);
            }

            getAllUpgrades().keySet().forEach(item -> {
                final var updateTag = item.createUpdateTag(this);
                if (updateTag != null) {
                    tag.put(BuiltInRegistries.ITEM.getKey(item).toString(), updateTag);
                }
            });
        });
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        processClientSync(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) processClientSync(pkt.getTag());
    }

    private void processClientSync(CompoundTag compound) {
        // called client-side on receipt of NBT
        if (compound.contains(CamouflageUpgrade.NBT_STATE_NAME)) {
            setCamouflage(NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), compound.getCompound(CamouflageUpgrade.NBT_STATE_NAME)));
        } else {
            setCamouflage(null);
        }

        energyStorage.updateForEnergyUpgrades(compound.getInt(NBT_ENERGY_UPGRADES));

        getAllUpgrades().keySet().forEach(item -> {
            final var updateTag = compound.get(BuiltInRegistries.ITEM.getKey(item).toString());
            item.processClientSync(this, (CompoundTag) updateTag);
        });
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        bufferHandler.deserializeNBT(nbt.getCompound(NBT_BUFFER));
        modulesHandler.deserializeNBT(nbt.getCompound(NBT_MODULES));
        upgradesHandler.deserializeNBT(nbt.getCompound(NBT_UPGRADES));
        energyStorage.deserializeNBT(nbt.getCompound(NBT_ENERGY));
        energyDirection = EnergyDirection.forValue(nbt.getString(NBT_ENERGY_DIR));
        redstoneBehaviour = RouterRedstoneBehaviour.forValue(nbt.getString(NBT_REDSTONE_MODE));
        active = nbt.getBoolean(NBT_ACTIVE);
        activeTimer = nbt.getInt(NBT_ACTIVE_TIMER);
        ecoMode = nbt.getBoolean(NBT_ECO_MODE);

        CompoundTag ext = new CompoundTag();
        CompoundTag ext1 = getExtensionData();
        for (String key : ext.getAllKeys()) {
            //noinspection ConstantConditions
            ext1.put(key, ext.get(key));
        }
        if (!ext.isEmpty()) nbt.put(NBT_EXTRA, ext);

        // When restoring, give the counter a random initial value to avoid all saved routers
        // having the same counter and firing simultaneously, which could conceivably cause lag
        // spikes if there are many routers in the world.
        // The -1 value indicates that a random value should be picked at the next compile.
        counter = -1;
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.put(NBT_BUFFER, bufferHandler.serializeNBT());
        if (hasItems(modulesHandler)) nbt.put(NBT_MODULES, modulesHandler.serializeNBT());
        if (hasItems(upgradesHandler)) nbt.put(NBT_UPGRADES, upgradesHandler.serializeNBT());
        if (redstoneBehaviour != RouterRedstoneBehaviour.ALWAYS) nbt.putString(NBT_REDSTONE_MODE, redstoneBehaviour.name());
        if (energyStorage.getCapacity() > 0) nbt.put(NBT_ENERGY, energyStorage.serializeNBT());
        if (energyDirection != EnergyDirection.FROM_ROUTER) nbt.putString(NBT_ENERGY_DIR, energyDirection.name());
        if (active) nbt.putBoolean(NBT_ACTIVE, true);
        if (activeTimer != 0) nbt.putInt(NBT_ACTIVE_TIMER, activeTimer);
        if (ecoMode) nbt.putBoolean(NBT_ECO_MODE, true);

        nbt.put(NBT_EXTRA, Util.make(new CompoundTag(), tag -> {
            CompoundTag ext1 = getExtensionData();
            //noinspection ConstantConditions
            ext1.getAllKeys().forEach(key -> tag.put(key, ext1.get(key)));
        }));
    }

    private boolean hasItems(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    public void clientTick() {
        for (Iterator<BeamData> iterator = beams.iterator(); iterator.hasNext(); ) {
            BeamData beam = iterator.next();
            beam.tick();
            if (beam.isExpired()) {
                iterator.remove();
                cachedRenderAABB = null;
            }
        }
    }

    public void serverTick() {
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
                ecoCounter = ConfigHolder.common.router.ecoTimeout.get();
            } else if (ecoCounter > 0) {
                ecoCounter--;
            }
        }

        maybeDoEnergyTransfer();
    }

    private void maybeDoEnergyTransfer() {
        if (getEnergyCapacity() > 0 && !getBufferItemStack().isEmpty() && redstoneBehaviour.shouldRun(getRedstonePower() > 0, false)) {
            IEnergyStorage energyHandler = bufferHandler.getEnergyStorage();
            if (energyHandler != null) {
                switch (energyDirection) {
                    case FROM_ROUTER -> {
                        int toExtract = getEnergyStorage().extractEnergy(getEnergyXferRate(), true);
                        int received = energyHandler.receiveEnergy(toExtract, false);
                        getEnergyStorage().extractEnergy(received, false);
                    }
                    case TO_ROUTER -> {
                        int toExtract = energyHandler.extractEnergy(getEnergyXferRate(), true);
                        int received = energyStorage.receiveEnergy(toExtract, false);
                        energyHandler.extractEnergy(received, false);
                    }
                }
            }
        }
    }

    public RouterFakePlayer getFakePlayer() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return null;

        if (fakePlayer == null) {
            fakePlayer = new RouterFakePlayer(this, serverLevel, getOwner());
            fakePlayer.getInventory().selected = 0;  // held item always in slot 0
            fakePlayer.setPosRaw(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        }
        return fakePlayer;
    }

    private GameProfile getOwner() {
        for (int i = 0; i < getUpgrades().getSlots(); i++) {
            ItemStack stack = getUpgrades().getStackInSlot(i);
            if (stack.getItem() instanceof SecurityUpgrade securityUpgrade) {
                Optional<GameProfile> opt = securityUpgrade.getOwnerProfile(stack);
                if (opt.isPresent()) return opt.get();
            }
        }
        return DEFAULT_FAKEPLAYER_PROFILE;
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

            newActive = runAllModules(powered, pulsed);

            if (!pendingBeams.isEmpty()) {
                PacketDistributor.TRACKING_CHUNK.with(nonNullLevel().getChunkAt(getBlockPos()))
                        .send(new ItemBeamMessage(this, pendingBeams));
                pendingBeams.clear();
            }
            if (prevCanEmit || canEmit) {
                handleRedstoneEmission();
            }
        }
        setActive(newActive);
        prevCanEmit = canEmit;
        executing = false;
    }

    private boolean runAllModules(boolean powered, boolean pulsed) {
        boolean newActive = false;

        for (CompiledIndexedModule cim : compiledModules) {
            CompiledModule cm = cim.compiledModule;
            if (cm != null && cm.hasTarget() && cm.getEnergyCost() <= getEnergyStorage().getEnergyStored() && cm.shouldRun(powered, pulsed)) {
                var event = cm.getEvent();
                if (event != null) {
                    event.setExecuted(false);
                    event.setCanceled(false);
                    NeoForge.EVENT_BUS.post(event);
                    if (event.isExecuted()) {
                        newActive = true;
                    }

                    if (event.isCanceled()) {
                        if ((newActive && cm.termination() == ModuleItem.Termination.RAN) || cm.termination() == ModuleItem.Termination.NOT_RAN) {
                            break;
                        }
                        continue;
                    }
                }

                if (cm.execute(this)) {
                    cm.getFilter().cycleRoundRobin().ifPresent(counter -> {
                        ItemStack moduleStack = modulesHandler.getStackInSlot(cim.index);
                        ModuleHelper.setRoundRobinCounter(moduleStack, counter);
                    });
                    getEnergyStorage().extractEnergy(cm.getEnergyCost(), false);
                    newActive = true;
                    if (cm.termination() == ModuleItem.Termination.RAN) {
                        break;
                    }
                } else if (cm.termination() == ModuleItem.Termination.NOT_RAN) {
                    break;
                }
            }
        }
        return newActive;
    }

    public int getTickRate() {
        return ecoMode && ecoCounter == 0 ? ConfigHolder.common.router.lowPowerTickRate.get() : tickRate;
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public void setRedstoneBehaviour(RouterRedstoneBehaviour redstoneBehaviour) {
        this.redstoneBehaviour = redstoneBehaviour;
        if (redstoneBehaviour == RouterRedstoneBehaviour.PULSE) {
            lastPower = getRedstonePower();
        }
        setChanged();
        handleSync(false);
    }

    private void setActive(boolean newActive) {
        if (active != newActive) {
            active = newActive;
            nonNullLevel().setBlock(getBlockPos(), getBlockState().setValue(ModularRouterBlock.ACTIVE,
                    newActive && getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 3), Block.UPDATE_CLIENTS);
            setChanged();
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
            ecoCounter = ConfigHolder.common.router.ecoTimeout.get();
            setChanged();
        }
    }

    @Override
    public BlockState getCamouflage() {
        return camouflage;
    }

    public void setCamouflage(BlockState newCamouflage) {
        if (newCamouflage != camouflage) {
            this.camouflage = newCamouflage;
            handleSync(true);
        }
    }

    private void handleSync(boolean renderUpdate) {
        // some tile entity field changed that the client needs to know about
        // if on server, sync TE data to client; if on client, possibly mark the TE for re-render
        Level level = nonNullLevel();
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        } else if (renderUpdate) {
            requestModelDataUpdate();
            level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
        }
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(CamouflageableBlock.CAMOUFLAGE_STATE, camouflage)
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
        nonNullLevel().updateNeighborsAt(worldPosition, state.getBlock());
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
                if (stack.getItem() instanceof ModuleItem moduleItem) {
                    CompiledModule cms = moduleItem.compile(this, stack);
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
        Level level = nonNullLevel();
        if (level.isClientSide || (recompileNeeded & COMPILE_UPGRADES) != 0) {
            int prevMufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
            upgradeCount.clear();
            permitted.clear();
            setCamouflage(null);
            tunedSyncValue = -1;
            for (int i = 0; i < N_UPGRADE_SLOTS; i++) {
                ItemStack stack = upgradesHandler.getStackInSlot(i);
                if (stack.getItem() instanceof UpgradeItem upgradeItem) {
                    upgradeCount.put(upgradeItem, getUpgradeCount(stack.getItem()) + stack.getCount());
                    upgradeItem.onCompiled(stack, this);
                }
            }

            itemsPerTick = 1 << (Math.min(6, getUpgradeCount(ModItems.STACK_UPGRADE.get())));
            tickRate = Math.max(ConfigHolder.common.router.hardMinTickRate.get(),
                    ConfigHolder.common.router.baseTickRate.get() - ConfigHolder.common.router.ticksPerUpgrade.get() * getUpgradeCount(ModItems.SPEED_UPGRADE.get()));
            fluidTransferRate = Math.min(ConfigHolder.common.router.fluidMaxTransferRate.get(),
                    ConfigHolder.common.router.fluidBaseTransferRate.get() + getUpgradeCount(ModItems.FLUID_UPGRADE.get()) * ConfigHolder.common.router.mBperFluidUpgade.get());

            energyStorage.updateForEnergyUpgrades(getUpgradeCount(ModItems.ENERGY_UPGRADE.get()));
            if (!level.isClientSide) {
                fakePlayer = null; // in case security upgrades change
                int mufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
                if (prevMufflers != mufflers) {
                    level.setBlock(worldPosition, getBlockState().setValue(ModularRouterBlock.ACTIVE, active && mufflers < 3), Block.UPDATE_CLIENTS);
                }
                notifyWatchingPlayers();
            }
        }
    }

    private void notifyWatchingPlayers() {
        for (Player player : nonNullLevel().players()) {
            if (player instanceof ServerPlayer sp && player.containerMenu instanceof RouterMenu c && c.getRouter() == this) {
                PacketDistributor.PLAYER.with(sp).send(RouterUpgradesSyncMessage.forRouter(this));
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
        nonNullLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(ModularRouterBlock.CAN_EMIT, canEmit));
    }

    public int getUpgradeCount(Item type) {
        return upgradeCount.getOrDefault(type, 0);
    }

    public Map<UpgradeItem, Integer> getAllUpgrades() {
        return Collections.unmodifiableMap(upgradeCount);
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
        int maxTransfer = ConfigHolder.common.router.baseTickRate.get() * fluidTransferRate;
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
            case IN -> {
                if (fluidTransferRemainingIn < amount)
                    ModularRouters.LOGGER.warn("fluid transfer: " + fluidTransferRemainingIn + " < " + amount);
                fluidTransferRemainingIn = Math.max(0, fluidTransferRemainingIn - amount);
            }
            case OUT -> {
                if (fluidTransferRemainingOut < amount)
                    ModularRouters.LOGGER.warn("fluid transfer: " + fluidTransferRemainingOut + " < " + amount);
                fluidTransferRemainingOut = Math.max(0, fluidTransferRemainingOut - amount);
            }
            default -> {
            }
        }
    }

    public Direction getAbsoluteFacing(RelativeDirection direction) {
        return direction.toAbsolute(getBlockState().getValue(ModularRouterBlock.FACING));
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
                allocateFluidTransfer(Math.min(pulseCounter, ConfigHolder.common.router.baseTickRate.get()));
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
            newRedstoneLevels[facing.get3DDataValue()] = power;
            newSignalType[facing.get3DDataValue()] = signalType;
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
            for (Direction dir : MiscUtil.DIRECTIONS) {
                if (signalType[dir.get3DDataValue()] == SignalType.STRONG) {
                    toNotify.add(dir.getOpposite());
                }
            }
            Arrays.fill(redstoneLevels, 0);
            Arrays.fill(signalType, SignalType.NONE);
        } else {
            for (Direction dir : MiscUtil.DIRECTIONS) {
                int i = dir.get3DDataValue();
                // if the signal op (strong/weak) has changed, notify neighbours of block in that direction
                if (newSignalType[i] != signalType[i]) {
                    toNotify.add(dir.getOpposite());
                    signalType[i] = newSignalType[i];
                }
                // if the signal strength has changed, notify immediate neighbours
                //   - and if signal op is strong, also notify neighbours of neighbour
                if (newRedstoneLevels[i] != redstoneLevels[i]) {
                    notifyOwnNeighbours = true;
                    if (newSignalType[i] == SignalType.STRONG) {
                        toNotify.add(dir.getOpposite());
                    }
                    redstoneLevels[i] = newRedstoneLevels[i];
                }
            }
        }

        Level level = nonNullLevel();
        for (Direction f : toNotify) {
            BlockPos pos2 = worldPosition.relative(f);
            level.updateNeighborsAt(pos2, level.getBlockState(pos2).getBlock());
        }
        if (notifyOwnNeighbours) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public void addPermittedIds(Set<UUID> permittedIds) {
        this.permitted.addAll(permittedIds);
    }

    public boolean isPermitted(Player player) {
        if (permitted.isEmpty() || permitted.contains(player.getUUID())) {
            return true;
        }
        return Arrays.stream(InteractionHand.values())
                .anyMatch(hand -> player.getItemInHand(hand).getItem() == ModItems.OVERRIDE_CARD.get());
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

    @SuppressWarnings("UnusedReturnValue")
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
        for (Direction facing : MiscUtil.DIRECTIONS) {
            if (getExtensionData().getInt(CompiledExtruderModule1.NBT_EXTRUDER_DIST + facing) > 0) {
                // ignore signal from any side we're extruding on (don't let placed redstone emitters lock up the router)
                continue;
            }
            int p = nonNullLevel().getSignal(pos.relative(facing), facing);
            if (p >= 15) {
                return p;
            } else if (p > power) {
                power = p;
            }
        }
        return power;
    }

    public CompoundTag getExtensionData() {
        if (extData == null) {
            extData = new CompoundTag();
        }
        return extData;
    }

    public void playSound(Player player, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
        if (getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) == 0) {
            nonNullLevel().playSound(player, pos, sound, category, volume, pitch);
        }
    }

    public void notifyModules() {
        compiledModules.forEach(cim -> cim.compiledModule.onNeighbourChange(this));
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.modularrouters.modular_router");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new RouterMenu(windowId, playerInventory, this.getBlockPos());
    }

    public GlobalPos getGlobalPos() {
        return MiscUtil.makeGlobalPos(nonNullLevel(), worldPosition);
    }

    /**
     * Set the upgrades to the given set.  Used client-side for upgrade sync'ing.
     *
     * @param upgradeCount item handler containing new set of upgrades
     */
    public void setUpgradesFrom(IItemHandler upgradeCount) {
        if (upgradeCount.getSlots() == upgradesHandler.getSlots()) {
            for (int i = 0; i < upgradeCount.getSlots(); i++) {
                upgradesHandler.setStackInSlot(i, upgradeCount.getStackInSlot(i).copy());
            }
        }
        compileUpgrades();
    }

    public AABB getRenderBoundingBox() {
        if (cachedRenderAABB == null) {
            cachedRenderAABB = new AABB(getBlockPos());
            beams.forEach(beam -> cachedRenderAABB = cachedRenderAABB.minmax(beam.getAABB(getBlockPos())));
        }
        return cachedRenderAABB;
    }

    public void addItemBeam(BeamData beamData) {
        if (nonNullLevel().isClientSide) {
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
        setChanged();
    }

    public EnergyDirection getEnergyDirection() {
        return energyDirection;
    }

    public int getModuleCount() {
        return compiledModules.size();
    }

    public IFluidHandlerItem getFluidHandler() {
        return bufferHandler.getFluidHandler();
    }

    public enum EnergyDirection implements TranslatableEnum {
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

            setChanged();
            recompileNeeded(flag);
        }
    }

    class ModuleHandler extends RouterItemHandler {
        ModuleHandler() {
            super(ModularRouterBlockEntity.COMPILE_MODULES, getModuleSlotCount(), s -> s.getItem() instanceof ModuleItem);
        }
    }

    class UpgradeHandler extends RouterItemHandler {
        UpgradeHandler() {
            super(ModularRouterBlockEntity.COMPILE_UPGRADES, getUpgradeSlotCount(), s -> s.getItem() instanceof UpgradeItem);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (!super.isItemValid(slot, stack)) return false;
            UpgradeItem item = (UpgradeItem) stack.getItem();
            for (int i = 0; i < getSlots(); i++) {
                ItemStack inSlot = getStackInSlot(i);
                if (inSlot.isEmpty() || slot == i) continue;
                // can't have the same upgrade in more than one slot
                // incompatible upgrades can't coexist
                if (stack.getItem() == inSlot.getItem() || !((UpgradeItem) inSlot.getItem()).isCompatibleWith(item)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof UpgradeItem u ? u.getStackLimit(slot) : 0;
        }
    }

    private record CompiledIndexedModule(CompiledModule compiledModule, int index) {
    }

    class RouterEnergyBuffer extends EnergyStorage {
        private int excess;  // "hidden" energy due to energy upgrades being removed

        public RouterEnergyBuffer(int capacity) {
            super(capacity);
            excess = 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!getRedstoneBehaviour().shouldRun(getRedstonePower() > 0, false)) {
                return 0;
            }
            int n = super.receiveEnergy(maxReceive, simulate);
            if (n != 0 && !simulate) setChanged();
            return n;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!getRedstoneBehaviour().shouldRun(getRedstonePower() > 0, false)) {
                return 0;
            }
            int n = super.extractEnergy(maxExtract, simulate);
            if (n != 0 && !simulate) setChanged();
            return n;
        }

        void updateForEnergyUpgrades(int nEnergyUpgrades) {
            int oldCapacity = capacity;
            capacity = ConfigHolder.common.router.fePerEnergyUpgrade.get() * nEnergyUpgrades;
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
            maxExtract = maxReceive = ConfigHolder.common.router.feXferPerEnergyUpgrade.get() * nEnergyUpgrades;
            if (oldCapacity == 0 && capacity != 0 || oldCapacity != 0 && capacity == 0) {
                // in case any pipes/cables need to connect/disconnect
                nonNullLevel().updateNeighborsAt(getBlockPos(), ModBlocks.MODULAR_ROUTER.get());
            }
        }

        public int getTransferRate() {
            return maxExtract;
        }

        @Override
        public Tag serializeNBT() {
            return Util.make(new CompoundTag(), tag -> {
                if (energy > 0) tag.putInt("Energy", energy);
                if (capacity > 0) tag.putInt("Capacity", capacity);
                if (excess > 0) tag.putInt("Excess", excess);
            });
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            if (!(nbt instanceof CompoundTag compound)) {
                throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
            }
            energy = compound.getInt("Energy");
            capacity = compound.getInt("Capacity");
            excess = compound.getInt("Excess");
        }

        public int getCapacity() {
            return capacity;
        }

        void setEnergyStored(int energyStored) {
            // only called client side for gui sync purposes
            this.energy = Math.min(energyStored, capacity);
        }
    }

    public class TrackedEnergy implements ContainerData {
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
