package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.*;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainerTypes {
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, ModularRouters.MODID);

    public static final RegistryObject<ContainerType<ContainerItemRouter>> CONTAINER_ITEM_ROUTER
            = register("item_router", ContainerItemRouter::new);

    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_BASIC
            = register("module_basic", ContainerModule::new);
    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_ACTIVATOR
            = register("module_activator", ContainerModules::createActivatorContainer);
    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_DETECTOR
            = register("module_detector", ContainerModules::createDetectorContainer);
    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_DISTRIBUTOR
            = register("module_distributor", ContainerModules::createDistributorContainer);
    public static final RegistryObject<ContainerType<ContainerExtruder2Module>> CONTAINER_MODULE_EXTRUDER2
            = register("module_extruder_2", ContainerModules::createExtruder2Container);
    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_FLINGER
            = register("module_flinger", ContainerModules::createFlingerContainer);
    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_FLUID
            = register("module_fluid", ContainerModules::createFluidContainer);
    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_PLAYER
            = register("module_player", ContainerModules::createPlayerContainer);
    public static final RegistryObject<ContainerType<ContainerModule>> CONTAINER_MODULE_VACUUM
            = register("module_vacuum", ContainerModules::createVacuumContainer);

    public static final RegistryObject<ContainerType<ContainerBulkItemFilter>> CONTAINER_BULK_ITEM_FILTER
            = register("bulk_item_filter", ContainerBulkItemFilter::new);
    public static final RegistryObject<ContainerType<ContainerModFilter>> CONTAINER_MOD_FILTER
            = register("mod_filter", ContainerModFilter::new);

    private static <C extends Container, T extends ContainerType<C>> RegistryObject<T> register(String name, IContainerFactory<? extends C> f) {
        //noinspection unchecked
        return CONTAINERS.register(name, () -> (T) IForgeContainerType.create(f));
    }
}