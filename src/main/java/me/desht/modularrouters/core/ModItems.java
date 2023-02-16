package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.item.augment.*;
import me.desht.modularrouters.item.module.*;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.item.smartfilter.InspectionFilter;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.item.smartfilter.RegexFilter;
import me.desht.modularrouters.item.upgrade.*;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraftforge.common.capabilities.CapabilityManager.get;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModularRouters.MODID);

    public static final List<RegistryObject<? extends Item>> REGISTRY_OBJECTS = new ArrayList<>();

    public static final RegistryObject<Item> BLANK_MODULE = register("blank_module");
    public static final RegistryObject<Item> BLANK_UPGRADE = register("blank_upgrade");
    public static final RegistryObject<Item> OVERRIDE_CARD = register("override_card");
    public static final RegistryObject<Item> AUGMENT_CORE = register("augment_core");

    public static final RegistryObject<Item> ACTIVATOR_MODULE = register("activator_module", ActivatorModule::new);
    public static final RegistryObject<Item> BREAKER_MODULE = register("breaker_module", BreakerModule::new);
    public static final RegistryObject<Item> CREATIVE_MODULE = register("creative_module", CreativeModule::new);
    public static final RegistryObject<Item> DETECTOR_MODULE = register("detector_module", DetectorModule::new);
    public static final RegistryObject<Item> DISTRIBUTOR_MODULE = register("distributor_module", DistributorModule::new);
    public static final RegistryObject<Item> DROPPER_MODULE = register("dropper_module", DropperModule::new);
    public static final RegistryObject<Item> ENERGY_DISTRIBUTOR_MODULE = register("energy_distributor_module", EnergyDistributorModule::new);
    public static final RegistryObject<Item> ENERGY_OUTPUT_MODULE = register("energy_output_module", EnergyOutputModule::new);
    public static final RegistryObject<Item> EXTRUDER_MODULE_1 = register("extruder_module_1", ExtruderModule1::new);
    public static final RegistryObject<Item> EXTRUDER_MODULE_2 = register("extruder_module_2", ExtruderModule2::new);
    public static final RegistryObject<Item> FLINGER_MODULE = register("flinger_module", FlingerModule::new);
    public static final RegistryObject<Item> FLUID_MODULE = register("fluid_module", FluidModule1::new);
    public static final RegistryObject<Item> FLUID_MODULE_2 = register("fluid_module_2", FluidModule2::new);
    public static final RegistryObject<Item> GAS_MODULE = register("gas_module", GasModule1::new);
    public static final RegistryObject<Item> GAS_MODULE_2 = register("gas_module_2", GasModule2::new);

    public static final RegistryObject<Item> PLACER_MODULE = register("placer_module", PlacerModule::new);
    public static final RegistryObject<Item> PLAYER_MODULE = register("player_module", PlayerModule::new);
    public static final RegistryObject<Item> PULLER_MODULE_1 = register("puller_module_1", PullerModule1::new);
    public static final RegistryObject<Item> PULLER_MODULE_2 = register("puller_module_2", PullerModule2::new);
    public static final RegistryObject<Item> SENDER_MODULE_1 = register("sender_module_1", SenderModule1::new);
    public static final RegistryObject<Item> SENDER_MODULE_2 = register("sender_module_2", SenderModule2::new);
    public static final RegistryObject<Item> SENDER_MODULE_3 = register("sender_module_3", SenderModule3::new);
    public static final RegistryObject<Item> VACUUM_MODULE = register("vacuum_module", VacuumModule::new);
    public static final RegistryObject<Item> VOID_MODULE = register("void_module", VoidModule::new);

    public static final RegistryObject<Item> BLAST_UPGRADE = register("blast_upgrade", BlastUpgrade::new);
    public static final RegistryObject<Item> CAMOUFLAGE_UPGRADE = register("camouflage_upgrade", CamouflageUpgrade::new);
    public static final RegistryObject<Item> ENERGY_UPGRADE = register("energy_upgrade", EnergyUpgrade::new);
    public static final RegistryObject<Item> FLUID_UPGRADE = register("fluid_upgrade", FluidUpgrade::new);

    public static final RegistryObject<Item> GAS_UPGRADE = register("gas_upgrade", GasUpgrade::new);
    public static final RegistryObject<Item> MUFFLER_UPGRADE = register("muffler_upgrade", MufflerUpgrade::new);
    public static final RegistryObject<Item> SECURITY_UPGRADE = register("security_upgrade", SecurityUpgrade::new);
    public static final RegistryObject<Item> SPEED_UPGRADE = register("speed_upgrade", SpeedUpgrade::new);
    public static final RegistryObject<Item> STACK_UPGRADE = register("stack_upgrade", StackUpgrade::new);
    public static final RegistryObject<Item> SYNC_UPGRADE = register("sync_upgrade", SyncUpgrade::new);

    public static final RegistryObject<Item> FAST_PICKUP_AUGMENT = register("fast_pickup_augment", FastPickupAugment::new);
    public static final RegistryObject<Item> FILTER_ROUND_ROBIN_AUGMENT = register("filter_round_robin_augment", FilterRoundRobinAugment::new);
    public static final RegistryObject<Item> MIMIC_AUGMENT = register("mimic_augment", MimicAugment::new);
    public static final RegistryObject<Item> PICKUP_DELAY_AUGMENT = register("pickup_delay_augment", PickupDelayAugment::new);
    public static final RegistryObject<Item> PUSHING_AUGMENT = register("pushing_augment", PushingAugment::new);
    public static final RegistryObject<Item> RANGE_DOWN_AUGMENT = register("range_down_augment", RangeAugments.RangeDownAugment::new);
    public static final RegistryObject<Item> RANGE_UP_AUGMENT = register("range_up_augment", RangeAugments.RangeUpAugment::new);
    public static final RegistryObject<Item> REDSTONE_AUGMENT = register("redstone_augment", RedstoneAugment::new);
    public static final RegistryObject<Item> REGULATOR_AUGMENT = register("regulator_augment", RegulatorAugment::new);
    public static final RegistryObject<Item> STACK_AUGMENT = register("stack_augment", StackAugment::new);
    public static final RegistryObject<Item> XP_VACUUM_AUGMENT = register("xp_vacuum_augment", XPVacuumAugment::new);

    public static final RegistryObject<Item> BULK_ITEM_FILTER = register("bulk_item_filter", BulkItemFilter::new);
    public static final RegistryObject<Item> INSPECTION_FILTER = register("inspection_filter", InspectionFilter::new);
    public static final RegistryObject<Item> MOD_FILTER = register("mod_filter", ModFilter::new);
    public static final RegistryObject<Item> REGEX_FILTER = register("regex_filter", RegexFilter::new);
    public static final Capability<IGasHandlerItem> GAS_HANDLER_ITEM = get(new CapabilityToken<>(){});
    public static final Capability<IGasHandler> GAS_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IGasHandler> GAS_HANDLER = get(new CapabilityToken<>(){});

    private Holder.Reference<Gas> gasDelegate;
    private boolean isEmpty;

    private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        RegistryObject<T> ro = ITEMS.register(name, sup);
        REGISTRY_OBJECTS.add(ro);
        return ro;
    }

    public static LazyOptional<IGasHandlerItem> getGasHandler(@NotNull ItemStack itemStack)
    {
        return itemStack.getCapability(ModItems.GAS_HANDLER_ITEM);
    }

    public interface IGasHandlerItem extends IGasHandler
    {
        /**
         * Get the container currently acted on by this gas handler.
         * The ItemStack may be different from its initial state, in the case of gas containers that have different gasses
         * for their filled and empty states.
         * May be an empty item if the container was drained and is consumable.
         */
        @NotNull
        ItemStack getContainer();
    }

    public static LazyOptional<IGasHandler> getGasHandler(Level level, BlockPos blockPos, @Nullable Direction side)
    {
        BlockState state = level.getBlockState(blockPos);
        if (state.hasBlockEntity())
        {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity != null)
            {
                return blockEntity.getCapability(ModItems.GAS_HANDLER_CAPABILITY, side);
            }
        }
        return LazyOptional.empty();
    }

/*
    public final Gas getGas()
    {
        return isEmpty ? MekanismAPI.EMPTY_GAS : gasDelegate.get();
    }
*/

    private static RegistryObject<Item> register(final String name) {
        return register(name, () -> new Item(defaultProps()));
    }

    static final CreativeModeTab MR_CREATIVE_TAB = new CreativeModeTab(ModularRouters.MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.MODULAR_ROUTER.get());
        }
    };

    public static Item.Properties defaultProps() {
        return new Item.Properties().tab(MR_CREATIVE_TAB);
    }

    public interface ITintable {
        TintColor getItemTint();
    }
}
