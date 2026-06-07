package me.desht.modularrouters.block.tile;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.api.event.RouterCompiledEvent;
import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.RouterMenu;
import me.desht.modularrouters.container.handler.BufferHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.event.TickEventHandler;
import me.desht.modularrouters.item.module.DetectorModule.SignalType;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.upgrade.CamouflageUpgrade;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule1;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.settings.ModuleTermination;
import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.logic.settings.TransferDirection;
import me.desht.modularrouters.network.messages.ItemBeamMessage;
import me.desht.modularrouters.network.messages.RouterUpgradesSyncMessage;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.TranslatableEnum;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ModularRouterBlockEntity extends BlockEntity implements ICamouflageable, MenuProvider {
    public static final NameAndId DEFAULT_FAKEPLAYER_PROFILE = new NameAndId(
            UUID.nameUUIDFromBytes(ModularRouters.MODID.getBytes()),
            "[" + ModularRouters.MODNAME + "]"
    );

    private static final int N_MODULE_SLOTS = 9;
    private static final int N_UPGRADE_SLOTS = 5;
    private static final int N_BUFFER_SLOTS = 1;

    private static final String NBT_ACTIVE = "Active";
    private static final String NBT_ACTIVE_TIMER = "ActiveTimer";
    private static final String NBT_ECO_MODE = "EcoMode";
    private static final String NBT_BUFFER = "Buffer";
    public static final String NBT_MODULES = "Modules";
    public static final String NBT_UPGRADES = "Upgrades";
    public static final String NBT_REDSTONE_MODE = "Redstone";
    private static final String NBT_ENERGY = "EnergyBuffer";
    private static final String NBT_ENERGY_DIR = "EnergyDirection";
    private static final String NBT_ENERGY_UPGRADES = "EnergyUpgrades";
    private static final String NBT_OWNER_PROFILE = "OwnerProfile";
    private static final String NBT_EXTRA = "Extra";

    private int counter = 0;
    private int pulseCounter = 0;

    private RedstoneBehaviour redstoneBehaviour = RedstoneBehaviour.ALWAYS;

    private final BufferHandler bufferHandler = new BufferHandler(this);
    private final ModuleHandler modulesHandler = new ModuleHandler();
    private final UpgradeHandler upgradesHandler = new UpgradeHandler();

    private final RouterEnergyBuffer energyStorage = new RouterEnergyBuffer(0);

    public final TrackedEnergy trackedEnergy = new TrackedEnergy();
    private EnergyDirection energyDirection = EnergyDirection.FROM_ROUTER;

    private final List<CompiledIndexedModule> compiledModules = new ArrayList<>();
    private final EnumSet<RecompileFlag> recompileNeeded = EnumSet.allOf(RecompileFlag.class);
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
    private boolean ecoMode = false;  // track eco-mode
    private int ecoCounter = ConfigHolder.common.router.ecoTimeout.get();
    private boolean hasPulsedModules = false;
    @Nullable
    private BlockState camouflage = null;  // block to masquerade as, set by Camo Upgrade
    private int tunedSyncValue = -1; // for synchronisation tuning, set by Sync Upgrade
    private boolean executing;       // are we currently executing modules?
    private boolean careAboutItemAttributes;  // whether to bother transferring item attributes to fake player
    private CompoundTag extData = new CompoundTag();  // extra (persisted) data which various modules can set & read

    public final List<BeamData> beams = new ArrayList<>(); // client-side: beams being rendered
    public final List<BeamData> pendingBeams = new ArrayList<>(); // server-side: beams to be sent to client

    public final Lazy<AABB> cachedRenderAABB = Lazy.of(this::buildCachedRenderAABB);

    @Nullable
    private NameAndId ownerID;
    @Nullable
    private RouterFakePlayer fakePlayer;

    public ModularRouterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MODULAR_ROUTER.get(), pos, state);
    }

    public BufferHandler getBuffer() {
        return bufferHandler;
    }

    public ModuleHandler getModules() {
        return modulesHandler;
    }

    public UpgradeHandler getUpgrades() {
        return upgradesHandler;
    }

    public Level nonNullLevel() {
        return Objects.requireNonNull(level);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return Util.make(new CompoundTag(), tag -> {
            if (camouflage != null) {
                tag.put(CamouflageUpgrade.NBT_STATE_NAME, NbtUtils.writeBlockState(camouflage));
            }

            // energy upgrade count sync'd so clientside TE knows if neighbouring cables should be able to connect
            int nEnergy = getUpgradeCount(ModItems.ENERGY_UPGRADE.get());
            if (nEnergy > 0) {
                tag.putInt(NBT_ENERGY_UPGRADES, nEnergy);
            }
        });
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);

        processClientSync(input);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
        processClientSync(valueInput);
    }

    public void setOwner(Player player) {
        ownerID = player.nameAndId();
        setChanged();
    }

    private void processClientSync(ValueInput input) {
        // called client-side on receipt of NBT
        input.read(CamouflageUpgrade.NBT_STATE_NAME, BlockState.CODEC).ifPresentOrElse(
                this::setCamouflage,
                () -> setCamouflage(null)
        );
        input.getInt(NBT_ENERGY_UPGRADES).ifPresent(energyStorage::updateForEnergyUpgrades);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        input.child(NBT_BUFFER).ifPresent(bufferHandler::deserialize);
        input.child(NBT_MODULES).ifPresent(modulesHandler::deserialize);
        input.child(NBT_UPGRADES).ifPresent(upgradesHandler::deserialize);
        input.child(NBT_ENERGY).ifPresent(energyStorage::deserialize);

        energyDirection = input.read(NBT_ENERGY_DIR, EnergyDirection.CODEC).orElse(EnergyDirection.TO_ROUTER);
        input.getString(NBT_REDSTONE_MODE).ifPresent(s -> redstoneBehaviour = RedstoneBehaviour.forValue(s));
        active = input.getBooleanOr(NBT_ACTIVE, false);
        activeTimer = input.getIntOr(NBT_ACTIVE_TIMER, 0);
        ecoMode = input.getBooleanOr(NBT_ECO_MODE, false);
        ownerID = input.read(NBT_OWNER_PROFILE, NameAndId.CODEC).orElse(DEFAULT_FAKEPLAYER_PROFILE);
        extData = input.read(NBT_EXTRA, CompoundTag.CODEC).orElseGet(CompoundTag::new);

        // When restoring, give the counter a random initial value to avoid all saved routers
        // having the same counter and firing simultaneously, which could conceivably cause lag
        // spikes if there are many routers in the world.
        // The -1 value indicates that a random value should be picked at the next compile.
        counter = -1;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putChild(NBT_BUFFER, bufferHandler);
        output.putChild(NBT_MODULES, modulesHandler);
        output.putChild(NBT_UPGRADES, upgradesHandler);
        if (energyStorage.getCapacityAsInt() > 0) output.putChild(NBT_ENERGY, energyStorage);

        if (redstoneBehaviour != RedstoneBehaviour.ALWAYS) output.putString(NBT_REDSTONE_MODE, redstoneBehaviour.name());
        if (energyDirection != EnergyDirection.FROM_ROUTER) output.store(NBT_ENERGY_DIR, EnergyDirection.CODEC, energyDirection);
        if (active) output.putBoolean(NBT_ACTIVE, true);
        if (activeTimer != 0) output.putInt(NBT_ACTIVE_TIMER, activeTimer);
        if (ecoMode) output.putBoolean(NBT_ECO_MODE, true);
        if (ownerID != null) {
            output.store(NBT_OWNER_PROFILE, NameAndId.CODEC, new NameAndId(ownerID.id(), ownerID.name()));
        }
        if (!getExtensionData().isEmpty()) output.store(NBT_EXTRA, CompoundTag.CODEC, getExtensionData());
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter getter) {
        super.applyImplicitComponents(getter);

        redstoneBehaviour = getter.getOrDefault(ModDataComponents.REDSTONE_BEHAVIOUR, RedstoneBehaviour.ALWAYS);
        modulesHandler.fillFrom(getter.getOrDefault(ModDataComponents.SAVED_MODULES, ItemContainerContents.EMPTY));
        upgradesHandler.fillFrom(getter.getOrDefault(ModDataComponents.SAVED_UPGRADES, ItemContainerContents.EMPTY));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);

        builder.set(ModDataComponents.REDSTONE_BEHAVIOUR, redstoneBehaviour);
        builder.set(ModDataComponents.SAVED_MODULES, modulesHandler.asContainerContents());
        builder.set(ModDataComponents.SAVED_UPGRADES, upgradesHandler.asContainerContents());
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        InventoryUtils.dropInventoryItems(nonNullLevel(), pos, getBuffer());
    }

    public void clientTick() {
        for (Iterator<BeamData> iterator = beams.iterator(); iterator.hasNext(); ) {
            BeamData beam = iterator.next();
            beam.tick();
            if (beam.isExpired()) {
                iterator.remove();
                cachedRenderAABB.invalidate();
            }
        }
    }

    public void serverTick() {
        if (!recompileNeeded.isEmpty()) {
            compile();
        }
        counter++;
        pulseCounter++;

        if (fakePlayer != null) {
            fakePlayer.tick();
            fakePlayer.getCooldowns().tick();
        }

        if (getRedstoneBehaviour() == RedstoneBehaviour.PULSE) {
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
            EnergyHandler energyHandler = bufferHandler.getEnergyStorage();
            if (energyHandler != null) {
                switch (energyDirection) {
                    case FROM_ROUTER -> EnergyHandlerUtil.move(energyStorage, energyHandler, getEnergyXferRate(), null);
                    case TO_ROUTER -> EnergyHandlerUtil.move(energyHandler, energyStorage, getEnergyXferRate(), null);
                }
            }
        }
    }

    public RouterFakePlayer getFakePlayer() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if (fakePlayer == null) {
                assert ownerID != null;
                fakePlayer = new RouterFakePlayer(this, serverLevel, new GameProfile(ownerID.id(), ownerID.name()));
                fakePlayer.getInventory().setSelectedSlot(0);  // held item always in slot 0
                fakePlayer.setPosRaw(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            }
            return fakePlayer;
        } else {
            throw new IllegalStateException("can't get fake player on the client!");
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

            newActive = runAllModules(powered, pulsed);

            if (!pendingBeams.isEmpty() && level instanceof ServerLevel serverLevel) {
                PacketDistributor.sendToPlayersTrackingChunk(serverLevel, ChunkPos.containing(getBlockPos()), ItemBeamMessage.create(getBlockPos(), pendingBeams));
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
            if (cm.shouldExecute() && cm.getEnergyCost() <= getEnergyStorage().getAmountAsInt() && cm.checkRedstone(powered, pulsed)) {
                var event = cm.getEvent();
                if (event != null) {
                    event.setExecuted(false);
                    event.setCanceled(false);
                    NeoForge.EVENT_BUS.post(event);
                    if (event.isExecuted()) {
                        newActive = true;
                    }

                    if (event.isCanceled()) {
                        if ((newActive && cm.termination() == ModuleTermination.RAN) || cm.termination() == ModuleTermination.NOT_RAN) {
                            break;
                        }
                        continue;
                    }
                }

                if (cm.execute(this)) {
                    cm.getFilter().cycleRoundRobin().ifPresent(counter -> {
                        ItemStack moduleStack = ItemUtil.getStack(modulesHandler, cim.index);
                        ModuleItem.setRoundRobinCounter(moduleStack, counter);
                        modulesHandler.setStackInSlot(cim.index, moduleStack);
                    });
                    try (var tx = Transaction.openRoot()) {
                        energyStorage.extract(cm.getEnergyCost(), tx);
                        tx.commit();
                    }
                    newActive = true;
                    if (cm.termination() == ModuleTermination.RAN) {
                        break;
                    }
                } else if (cm.termination() == ModuleTermination.NOT_RAN) {
                    break;
                }
            }
        }
        return newActive;
    }

    public int getTickRate() {
        return ecoMode && ecoCounter == 0 ? ConfigHolder.common.router.lowPowerTickRate.get() : tickRate;
    }

    public RedstoneBehaviour getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public void setRedstoneBehaviour(RedstoneBehaviour redstoneBehaviour) {
        this.redstoneBehaviour = redstoneBehaviour;
        if (redstoneBehaviour == RedstoneBehaviour.PULSE) {
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

    public void setEcoMode(boolean newEco) {
        if (newEco != ecoMode) {
            ecoMode = newEco;
            ecoCounter = ConfigHolder.common.router.ecoTimeout.get();
            setChanged();
        }
    }

    @Override
    public @Nullable BlockState getCamouflage() {
        return camouflage;
    }

    public void setCamouflage(@Nullable BlockState newCamouflage) {
        if (newCamouflage != camouflage) {
            this.camouflage = newCamouflage;
            handleSync(true);
        }
    }

    private void handleSync(boolean renderUpdate) {
        // some tile entity field changed that the client needs to know about
        // if on server, sync TE data to client; if on client, possibly mark the TE for re-render
        Level level = nonNullLevel();
        if (!level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        } else if (renderUpdate) {
            requestModelDataUpdate();
            level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
        }
    }

    @Override
    public ModelData getModelData() {
        return ICamouflageable.makeModelData(camouflage);
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
            counter = nonNullLevel().getRandom().nextInt(tickRate);
        }

        BlockState state = getBlockState();
        nonNullLevel().updateNeighborsAt(worldPosition, state.getBlock());
        setChanged();
        recompileNeeded.clear();
    }

    private void compileModules() {
        if (recompileNeeded.contains(RecompileFlag.MODULES)) {
            setHasPulsedModules(false);
            for (CompiledIndexedModule cim : compiledModules) {
                cim.compiledModule.cleanup(this);
            }
            compiledModules.clear();
            careAboutItemAttributes = false;
            for (int i = 0; i < N_MODULE_SLOTS; i++) {
                ItemStack stack = ItemUtil.getStack(modulesHandler, i);
                if (stack.getItem() instanceof ModuleItem moduleItem) {
                    CompiledModule cms = moduleItem.compile(this, stack);
                    compiledModules.add(new CompiledIndexedModule(cms, i));
                    cms.onCompiled(this);
                    if (cms.careAboutItemAttributes()) careAboutItemAttributes = true;
                }
            }
            NeoForge.EVENT_BUS.post(new RouterCompiledEvent.Modules(this));
        }
    }

    private void compileUpgrades() {
        // if called client-side, always recompile (it's due to an upgrade sync)
        Level level = nonNullLevel();
        if (level.isClientSide() || recompileNeeded.contains(RecompileFlag.UPGRADES)) {
            int prevMufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
            upgradeCount.clear();
            permitted.clear();
            setCamouflage(null);
            tunedSyncValue = -1;
            for (int i = 0; i < N_UPGRADE_SLOTS; i++) {
                ItemStack stack = ItemUtil.getStack(upgradesHandler, i);
                if (stack.getItem() instanceof UpgradeItem upgradeItem) {
                    upgradeCount.put(upgradeItem, getUpgradeCount(upgradeItem) + stack.getCount());
                    upgradeItem.onCompiled(stack, this);
                }
            }

            itemsPerTick = 1 << (Math.min(6, getUpgradeCount(ModItems.STACK_UPGRADE.get())));
            tickRate = Math.max(ConfigHolder.common.router.hardMinTickRate.get(),
                    ConfigHolder.common.router.baseTickRate.get() - ConfigHolder.common.router.ticksPerUpgrade.get() * getUpgradeCount(ModItems.SPEED_UPGRADE.get()));
            fluidTransferRate = Math.min(ConfigHolder.common.router.fluidMaxTransferRate.get(),
                    ConfigHolder.common.router.fluidBaseTransferRate.get() + getUpgradeCount(ModItems.FLUID_UPGRADE.get()) * ConfigHolder.common.router.mBperFluidUpgrade.get());

            energyStorage.updateForEnergyUpgrades(getUpgradeCount(ModItems.ENERGY_UPGRADE.get()));
            if (!level.isClientSide()) {
                int mufflers = getUpgradeCount(ModItems.MUFFLER_UPGRADE.get());
                if (prevMufflers != mufflers) {
                    level.setBlock(worldPosition, getBlockState().setValue(ModularRouterBlock.ACTIVE, active && mufflers < 3), Block.UPDATE_CLIENTS);
                }
                notifyWatchingPlayers();
            }

            NeoForge.EVENT_BUS.post(new RouterCompiledEvent.Upgrades(this));
        }
    }

    public int getModuleCount() {
        return compiledModules.size();
    }

    private void notifyWatchingPlayers() {
        for (Player player : nonNullLevel().players()) {
            if (player instanceof ServerPlayer sp && player.containerMenu instanceof RouterMenu c && c.getRouter() == this) {
                PacketDistributor.sendToPlayer(sp, RouterUpgradesSyncMessage.forRouter(this));
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
//        ModularRouters.LOGGER.info("sync counter for {}: tc={} ct={} tuning={} delta={} counter={}", getBlockPos(), TickEventHandler.TickCounter, compileTime,tuning, delta, tickRate - delta);
        return tickRate - delta;
    }

    public void setAllowRedstoneEmission(boolean allow) {
        canEmit = allow;
        nonNullLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(ModularRouterBlock.CAN_EMIT, canEmit));
    }

    public int getUpgradeCount(UpgradeItem type) {
        return upgradeCount.getOrDefault(type, 0);
    }

    public void recompileNeeded(RecompileFlag what) {
        recompileNeeded.add(what);
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

    public int getCurrentFluidTransferAllowance(TransferDirection dir) {
        return dir == TransferDirection.TO_ROUTER ? fluidTransferRemainingIn : fluidTransferRemainingOut;
    }

    public void transferredFluid(int amount, TransferDirection dir) {
        switch (dir) {
            case TO_ROUTER -> {
                if (fluidTransferRemainingIn < amount)
                    ModularRouters.LOGGER.warn("fluid transfer to router: {} < {}", fluidTransferRemainingIn, amount);
                fluidTransferRemainingIn = Math.max(0, fluidTransferRemainingIn - amount);
            }
            case FROM_ROUTER -> {
                if (fluidTransferRemainingOut < amount)
                    ModularRouters.LOGGER.warn("fluid transfer from router: {} < {}", fluidTransferRemainingOut, amount);
                fluidTransferRemainingOut = Math.max(0, fluidTransferRemainingOut - amount);
            }
            default -> {
            }
        }
    }

    public Direction getAbsoluteFacing(RelativeDirection direction) {
        return direction.toAbsolute(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    public ItemStack getBufferItemStack() {
        return bufferHandler.getStackInSlot(0);
    }

    public void checkForRedstonePulse() {
        redstonePower = calculateIncomingRedstonePower(worldPosition);
        if (!executing) {
            // avoid dangerous recursion from an executing module triggering more block updates during execution
            if (redstoneBehaviour == RedstoneBehaviour.PULSE
                    || hasPulsedModules && redstoneBehaviour == RedstoneBehaviour.ALWAYS) {
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
        ItemResource resource = bufferHandler.getResource(0);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        try (var tx = Transaction.openRoot()) {
            int extracted = bufferHandler.extract(0, resource, amount, tx);
            // don't commit - simulation only
            return extracted > 0 ? resource.toStack(extracted) : ItemStack.EMPTY;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ItemStack extractBuffer(int amount) {
        ItemResource resource = bufferHandler.getResource(0);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        try (var tx = Transaction.openRoot()) {
            int extracted = bufferHandler.extract(0, resource, amount, tx);
            tx.commit();
            return extracted > 0 ? resource.toStack(extracted) : ItemStack.EMPTY;
        }
    }

    public ItemStack insertBuffer(ItemStack stack) {
        return ItemUtil.insertItemReturnRemaining(bufferHandler, 0, stack, false, null);
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
            if (getExtensionData().getIntOr(CompiledExtruderModule1.NBT_EXTRUDER_DIST + facing, 0) > 0) {
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
        return extData;
    }

    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
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
     * @param upgradeHandler item handler containing new set of upgrades
     */
    public void setUpgradesFrom(ResourceHandler<ItemResource> upgradeHandler) {
        if (upgradeHandler.size() == upgradesHandler.size()) {
            for (int i = 0; i < upgradeHandler.size(); i++) {
                upgradesHandler.setStackInSlot(i, ItemUtil.getStack(upgradeHandler, i).copy());
            }
        }
        compileUpgrades();
    }


    private AABB buildCachedRenderAABB() {
        AABB box = new AABB(getBlockPos());
        for (BeamData beam : beams) {
            box = box.minmax(beam.getAABB(getBlockPos()));
        }
        return box;
    }

    public void addItemBeam(BeamData beamData) {
        if (nonNullLevel().isClientSide()) {
            beams.add(beamData);
            cachedRenderAABB.invalidate();
        } else {
            pendingBeams.add(beamData);
        }
    }

    public int getEnergyCapacity() {
        return energyStorage.getCapacityAsInt();
    }

    public int getEnergyXferRate() {
        return energyStorage.getTransferRate();
    }

    public RouterEnergyBuffer getEnergyStorage() {
        return energyStorage;
    }

    public void setEnergyDirection(EnergyDirection energyDirection) {
        this.energyDirection = energyDirection;
        setChanged();
    }

    public EnergyDirection getEnergyDirection() {
        return energyDirection;
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler() {
        return bufferHandler.getFluidHandler();
    }

    public AABB getRenderBoundingBox() {
        return cachedRenderAABB.get();
    }

    public enum EnergyDirection implements TranslatableEnum, StringRepresentable {
        FROM_ROUTER("from_router"),
        TO_ROUTER("to_router"),
        NONE("none");

        public static final Codec<EnergyDirection> CODEC = StringRepresentable.fromEnum(EnergyDirection::values);

        private final String name;

        EnergyDirection(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.tooltip.energy." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    abstract class RouterItemHandler extends ItemStacksResourceHandler {
        private final Predicate<ItemStack> validator;
        private final RecompileFlag flag;

        private RouterItemHandler(RecompileFlag flag, int size, Predicate<ItemStack> validator) {
            super(size);
            this.validator = validator;
            this.flag = flag;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !resource.isEmpty() && validator.test(resource.toStack());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            recompileNeeded(flag);
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            return 1;
        }

        public ItemContainerContents asContainerContents() {
            return ItemContainerContents.fromItems(stacks);
        }

        public void fillFrom(ItemContainerContents contents) {
            contents.copyInto(stacks);
        }

        public void setStackInSlot(int slot, ItemStack stack) {
            set(slot, ItemResource.of(stack), stack.getCount());
        }

        public ItemStack getStackInSlot(int slot) {
            return ItemUtil.getStack(this, slot);
        }

        public int getSlots() {
            return size();
        }
    }

    public class ModuleHandler extends RouterItemHandler {
        ModuleHandler() {
            super(RecompileFlag.MODULES, getModuleSlotCount(), s -> s.getItem() instanceof ModuleItem);
        }
    }

    public class UpgradeHandler extends RouterItemHandler {
        UpgradeHandler() {
            super(RecompileFlag.UPGRADES, getUpgradeSlotCount(), s -> s.getItem() instanceof UpgradeItem);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (!super.isValid(index, resource)) return false;
            ItemStack stack = resource.toStack();
            UpgradeItem item = (UpgradeItem) stack.getItem();
            for (int i = 0; i < size(); i++) {
                ItemStack inSlot = ItemUtil.getStack(this, i);
                if (inSlot.isEmpty() || index == i) continue;
                // can't have the same upgrade in more than one slot
                // incompatible upgrades can't coexist
                if (stack.getItem() == inSlot.getItem() || !((UpgradeItem) inSlot.getItem()).isCompatibleWith(item) || !item.isCompatibleWith((UpgradeItem) inSlot.getItem())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
            if (resource.isEmpty()) return 64;
            return resource.getItem() instanceof UpgradeItem u ? u.getInstalledStackLimit() : 0;
        }
    }

    private record CompiledIndexedModule(CompiledModule compiledModule, int index) {
    }

    public class RouterEnergyBuffer extends SimpleEnergyHandler {
        private int excess;  // "hidden" energy due to energy upgrades being removed

        public RouterEnergyBuffer(int capacity) {
            super(capacity);
            excess = 0;
        }

        @Override
        public int insert(int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
            if (!getRedstoneBehaviour().shouldRun(getRedstonePower() > 0, false)) {
                return 0;
            }
            return super.insert(amount, transaction);
        }

        @Override
        public int extract(int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext transaction) {
            if (!getRedstoneBehaviour().shouldRun(getRedstonePower() > 0, false)) {
                return 0;
            }
            return super.extract(amount, transaction);
        }

        @Override
        protected void onEnergyChanged(int previousAmount) {
            setChanged();
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
            maxExtract = maxInsert = ConfigHolder.common.router.feXferPerEnergyUpgrade.get() * nEnergyUpgrades;
            if (oldCapacity == 0 && capacity != 0 || oldCapacity != 0 && capacity == 0) {
                // in case any pipes/cables need to connect/disconnect
                nonNullLevel().updateNeighborsAt(getBlockPos(), ModBlocks.MODULAR_ROUTER.get());
            }
        }

        public int getTransferRate() {
            return maxExtract;
        }

        @Override
        public void serialize(ValueOutput output) {
            if (energy > 0) output.putInt("Energy", energy);
            if (capacity > 0) output.putInt("Capacity", capacity);
            if (excess > 0) output.putInt("Excess", excess);
        }

        @Override
        public void deserialize(ValueInput input) {
            energy = input.getIntOr("Energy", 0);
            capacity = input.getIntOr("Capacity", 0);
            excess = input.getIntOr("Excess", 0);
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
                res = energyStorage.getAmountAsInt() & 0x0000FFFF;
            } else if (idx == 1) {
                res = (energyStorage.getAmountAsInt() & 0xFFFF0000) >> 16;
            }
            return res;
        }

        @Override
        public void set(int idx, int val) {
            if (val < 0) val += 65536;  // due to int->short conversion silliness in SWindowPropertyPacket
            if (idx == 0) {
                energyStorage.setEnergyStored(energyStorage.getAmountAsInt() & 0xFFFF0000 | val);
            } else if (idx == 1) {
                energyStorage.setEnergyStored(energyStorage.getAmountAsInt() & 0x0000FFFF | val << 16);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public enum RecompileFlag {
        MODULES,
        UPGRADES
    }
}
