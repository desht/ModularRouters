package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ModularRouters.MODID);

    public static final RegistryObject<MenuType<RouterMenu>> ROUTER_MENU
            = register("modular_router", RouterMenu::new);

    public static final RegistryObject<MenuType<ModuleMenu>> BASE_MODULE_MENU
            = register("module_basic", ModuleMenu::new);
    public static final RegistryObject<MenuType<ModuleMenu>> ACTIVATOR_MENU
            = register("module_activator", ModuleMenuFactories::createActivatorMenu);
    public static final RegistryObject<MenuType<ModuleMenu>> BREAKER_MENU
            = register("module_breaker", ModuleMenuFactories::createBreakerMenu);
    public static final RegistryObject<MenuType<ModuleMenu>> DETECTOR_MENU
            = register("module_detector", ModuleMenuFactories::createDetectorMenu);
    public static final RegistryObject<MenuType<ModuleMenu>> DISTRIBUTOR_MENU
            = register("module_distributor", ModuleMenuFactories::createDistributorMenu);
    public static final RegistryObject<MenuType<Extruder2ModuleMenu>> EXTRUDER2_MENU
            = register("module_extruder_2", ModuleMenuFactories::createExtruder2Menu);
    public static final RegistryObject<MenuType<ModuleMenu>> FLINGER_MENU
            = register("module_flinger", ModuleMenuFactories::createFlingerMenu);
    public static final RegistryObject<MenuType<ModuleMenu>> FLUID_MENU
            = register("module_fluid", ModuleMenuFactories::createFluidMenu);
    public static final RegistryObject<MenuType<ModuleMenu>> PLAYER_MENU
            = register("module_player", ModuleMenuFactories::createPlayerMenu);
    public static final RegistryObject<MenuType<ModuleMenu>> VACUUM_MENU
            = register("module_vacuum", ModuleMenuFactories::createVacuumMenu);

    public static final RegistryObject<MenuType<BulkItemFilterMenu>> BULK_FILTER_MENU
            = register("bulk_item_filter", BulkItemFilterMenu::new);
    public static final RegistryObject<MenuType<ModFilterMenu>> MOD_FILTER_MENU
            = register("mod_filter", ModFilterMenu::new);
    public static final RegistryObject<MenuType<TagFilterMenu>> TAG_FILTER_MENU
            = register("tag_filter", TagFilterMenu::new);

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> RegistryObject<T> register(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return MENUS.register(name, () -> (T) IForgeMenuType.create(f));
    }
}