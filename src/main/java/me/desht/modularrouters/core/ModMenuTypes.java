package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModularRouters.MODID);

    public static final Supplier<MenuType<RouterMenu>> ROUTER_MENU
            = register("modular_router", RouterMenu::new);

    public static final Supplier<MenuType<ModuleMenu>> BASE_MODULE_MENU
            = register("module_basic", ModuleMenu::new);
    public static final Supplier<MenuType<ModuleMenu>> ACTIVATOR_MENU
            = register("module_activator", ModuleMenuFactories::createActivatorMenu);
    public static final Supplier<MenuType<ModuleMenu>> BREAKER_MENU
            = register("module_breaker", ModuleMenuFactories::createBreakerMenu);
    public static final Supplier<MenuType<ModuleMenu>> DETECTOR_MENU
            = register("module_detector", ModuleMenuFactories::createDetectorMenu);
    public static final Supplier<MenuType<ModuleMenu>> DISTRIBUTOR_MENU
            = register("module_distributor", ModuleMenuFactories::createDistributorMenu);
    public static final Supplier<MenuType<Extruder2ModuleMenu>> EXTRUDER2_MENU
            = register("module_extruder_2", ModuleMenuFactories::createExtruder2Menu);
    public static final Supplier<MenuType<ModuleMenu>> FLINGER_MENU
            = register("module_flinger", ModuleMenuFactories::createFlingerMenu);
    public static final Supplier<MenuType<ModuleMenu>> FLUID_MENU
            = register("module_fluid", ModuleMenuFactories::createFluidMenu);
    public static final Supplier<MenuType<ModuleMenu>> PLAYER_MENU
            = register("module_player", ModuleMenuFactories::createPlayerMenu);
    public static final Supplier<MenuType<ModuleMenu>> VACUUM_MENU
            = register("module_vacuum", ModuleMenuFactories::createVacuumMenu);

    public static final Supplier<MenuType<BulkItemFilterMenu>> BULK_FILTER_MENU
            = register("bulk_item_filter", BulkItemFilterMenu::new);
    public static final Supplier<MenuType<ModFilterMenu>> MOD_FILTER_MENU
            = register("mod_filter", ModFilterMenu::new);
    public static final Supplier<MenuType<TagFilterMenu>> TAG_FILTER_MENU
            = register("tag_filter", TagFilterMenu::new);

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> Supplier<T> register(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return MENUS.register(name, () -> (T) IMenuTypeExtension.create(f));
    }
}