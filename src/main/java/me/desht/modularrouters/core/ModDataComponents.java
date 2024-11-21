package me.desht.modularrouters.core;

import com.mojang.serialization.Codec;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.item.upgrade.SecurityUpgrade;
import me.desht.modularrouters.logic.ModuleTargetList;
import me.desht.modularrouters.logic.compiled.*;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.ComparisonList;
import me.desht.modularrouters.logic.settings.ModuleSettings;
import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS
            = DeferredRegister.createDataComponents(ModularRouters.MODID);

    // a list of targets for a targeted module (usually one target, but distributor modules can have multiple)
    public static final Supplier<DataComponentType<ModuleTargetList>> MODULE_TARGET_LIST
            = COMPONENTS.registerComponentType("module_target_list", builder -> builder
            .persistent(ModuleTargetList.CODEC)
            .networkSynchronized(ModuleTargetList.STREAM_CODEC)
    );
    // module settings common to ALL modules
    public static final Supplier<DataComponentType<ModuleSettings>> COMMON_MODULE_SETTINGS
            = COMPONENTS.registerComponentType("common_module_settings", builder -> builder
            .persistent(ModuleSettings.CODEC)
            .networkSynchronized(ModuleSettings.STREAM_CODEC)
    );
    // module-specific settings in the next bunch of components...
    public static final Supplier<DataComponentType<CompiledActivatorModule.ActivatorSettings>> ACTIVATOR_SETTINGS
            = COMPONENTS.registerComponentType("activator_settings", builder -> builder
            .persistent(CompiledActivatorModule.ActivatorSettings.CODEC)
            .networkSynchronized(CompiledActivatorModule.ActivatorSettings.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<CompiledBreakerModule.BreakerSettings>> BREAKER_SETTINGS
            = COMPONENTS.registerComponentType("breaker_settings", builder -> builder
            .persistent(CompiledBreakerModule.BreakerSettings.CODEC)
            .networkSynchronized(CompiledBreakerModule.BreakerSettings.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<CompiledDetectorModule.DetectorSettings>> DETECTOR_SETTINGS
            = COMPONENTS.registerComponentType("detector_settings", builder -> builder
            .persistent(CompiledDetectorModule.DetectorSettings.CODEC)
            .networkSynchronized(CompiledDetectorModule.DetectorSettings.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<CompiledDistributorModule.DistributorSettings>> DISTRIBUTOR_SETTINGS
            = COMPONENTS.registerComponentType("distributor_settings", builder -> builder
            .persistent(CompiledDistributorModule.DistributorSettings.CODEC)
            .networkSynchronized(CompiledDistributorModule.DistributorSettings.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<CompiledFlingerModule.FlingerSettings>> FLINGER_SETTINGS
            = COMPONENTS.registerComponentType("flinger_settings", builder -> builder
            .persistent(CompiledFlingerModule.FlingerSettings.CODEC)
            .networkSynchronized(CompiledFlingerModule.FlingerSettings.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<CompiledFluidModule.FluidModuleSettings>> FLUID_SETTINGS
            = COMPONENTS.registerComponentType("fluid_settings", builder -> builder
            .persistent(CompiledFluidModule.FluidModuleSettings.CODEC)
            .networkSynchronized(CompiledFluidModule.FluidModuleSettings.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<CompiledPlayerModule.PlayerSettings>> PLAYER_SETTINGS
            = COMPONENTS.registerComponentType("player_settings", builder -> builder
            .persistent(CompiledPlayerModule.PlayerSettings.CODEC)
            .networkSynchronized(CompiledPlayerModule.PlayerSettings.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<CompiledVacuumModule.VacuumSettings>> VACUUM_SETTINGS
            = COMPONENTS.registerComponentType("vacuum_settings", builder -> builder
            .persistent(CompiledVacuumModule.VacuumSettings.CODEC)
            .networkSynchronized(CompiledVacuumModule.VacuumSettings.STREAM_CODEC)
    );
    // a list of filter strings on a mod/tag/regex filter
    public static final Supplier<DataComponentType<List<String>>> FILTER_STRINGS
            = COMPONENTS.registerComponentType("filter_strings", builder -> builder
            .persistent(Codec.STRING.listOf(0, ModFilter.MAX_SIZE))
            .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(ModFilter.MAX_SIZE)))
    );
    // a list of comparison objects on an inspection filter
    public static final Supplier<DataComponentType<ComparisonList>> COMPARISON_LIST
            = COMPONENTS.registerComponentType("comparison_list", builder -> builder
            .persistent(ComparisonList.CODEC)
            .networkSynchronized(ComparisonList.STREAM_CODEC)
    );
    // pickaxe stored in a Breaker or Extruder Mk1
    public static final Supplier<DataComponentType<ItemContainerContents>> PICKAXE
            = COMPONENTS.registerComponentType("pickaxe", builder -> builder
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
    );
    // a collection of itemstacks in a module, used for the filter
    public static final Supplier<DataComponentType<ItemContainerContents>> FILTER
            = COMPONENTS.registerComponentType("filter", builder -> builder
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
    );
    // the augment items in a module
    public static final Supplier<DataComponentType<ItemContainerContents>> AUGMENTS
            = COMPONENTS.registerComponentType("augments", builder -> builder
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
    );
    // the template items in an Extruder Mk2
    public static final Supplier<DataComponentType<ItemContainerContents>> EXTRUDER2_TEMPLATE
            = COMPONENTS.registerComponentType("ex2_template", builder -> builder
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
    );
    // the owner of a player module or security upgrade
    public static final Supplier<DataComponentType<ResolvableProfile>> OWNER
            = COMPONENTS.registerComponentType("security_owner", builder -> builder
            .persistent(ResolvableProfile.CODEC)
            .networkSynchronized(ResolvableProfile.STREAM_CODEC)
    );
    // list of permitted players on a security upgrade
    public static final Supplier<DataComponentType<SecurityUpgrade.SecurityList>> SECURITY_LIST
            = COMPONENTS.registerComponentType("security_list", builder -> builder
            .persistent(SecurityUpgrade.SecurityList.CODEC)
            .networkSynchronized(SecurityUpgrade.SecurityList.STREAM_CODEC)
    );
    // the camouflage blockstate on a camouflage upgrade
    public static final Supplier<DataComponentType<BlockState>> CAMOUFLAGE
            = COMPONENTS.registerComponentType("camouflage", builder -> builder
            .persistent(BlockState.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(BlockState.CODEC))
    );
    // the tuning value for a sync upgrade
    public static final Supplier<DataComponentType<Integer>> SYNC_TUNING
            = COMPONENTS.registerComponentType("sync_tuning", builder -> builder
            .persistent(ExtraCodecs.intRange(1, 20))
            .networkSynchronized(ByteBufCodecs.VAR_INT)
    );
    // the current round-robin value for a module with filter in round-robin mode
    public static final Supplier<DataComponentType<Integer>> RR_COUNTER
            = COMPONENTS.registerComponentType("rr_counter", builder -> builder
            .persistent(ExtraCodecs.NON_NEGATIVE_INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
    );

    // redstone behaviour stored on a dropped router item
    public static final Supplier<DataComponentType<RedstoneBehaviour>> REDSTONE_BEHAVIOUR
            = COMPONENTS.registerComponentType("saved_redstone", builder -> builder
            .persistent(StringRepresentable.fromEnum(RedstoneBehaviour::values))
            .networkSynchronized(NeoForgeStreamCodecs.enumCodec(RedstoneBehaviour.class))
    );
    // saved modules stored on a dropped router item
    public static final Supplier<DataComponentType<ItemContainerContents>> SAVED_MODULES
            = COMPONENTS.registerComponentType("saved_modules", builder -> builder
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
    );
    // saved upgrades stored on a dropped router item
    public static final Supplier<DataComponentType<ItemContainerContents>> SAVED_UPGRADES
            = COMPONENTS.registerComponentType("saved_upgrades", builder -> builder
            .persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
    );
}
