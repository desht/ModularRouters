package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, ModularRouters.MODID);

    public static final RegistryObject<MenuType<ContainerModularRouter>> CONTAINER_ITEM_ROUTER
            = register("modular_router", ContainerModularRouter::new);

    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_BASIC
            = register("module_basic", ContainerModule::new);
    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_ACTIVATOR
            = register("module_activator", ContainerModules::createActivatorContainer);
    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_DETECTOR
            = register("module_detector", ContainerModules::createDetectorContainer);
    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_DISTRIBUTOR
            = register("module_distributor", ContainerModules::createDistributorContainer);
    public static final RegistryObject<MenuType<ContainerExtruder2Module>> CONTAINER_MODULE_EXTRUDER2
            = register("module_extruder_2", ContainerModules::createExtruder2Container);
    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_FLINGER
            = register("module_flinger", ContainerModules::createFlingerContainer);
    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_FLUID
            = register("module_fluid", ContainerModules::createFluidContainer);
    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_PLAYER
            = register("module_player", ContainerModules::createPlayerContainer);
    public static final RegistryObject<MenuType<ContainerModule>> CONTAINER_MODULE_VACUUM
            = register("module_vacuum", ContainerModules::createVacuumContainer);

    public static final RegistryObject<MenuType<ContainerBulkItemFilter>> CONTAINER_BULK_ITEM_FILTER
            = register("bulk_item_filter", ContainerBulkItemFilter::new);
    public static final RegistryObject<MenuType<ContainerModFilter>> CONTAINER_MOD_FILTER
            = register("mod_filter", ContainerModFilter::new);

    private static <C extends AbstractContainerMenu, T extends MenuType<C>> RegistryObject<T> register(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return CONTAINERS.register(name, () -> (T) IForgeContainerType.create(f));
    }
}