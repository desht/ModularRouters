package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.client.gui.filter.GuiModFilter;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.container.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.modularrouters.util.MiscUtil.RL;

@ObjectHolder(ModularRouters.MODID)
public class ModContainerTypes {
    public static final ContainerType<ContainerItemRouter> CONTAINER_ITEM_ROUTER = null;

    public static final ContainerType<ContainerModule> CONTAINER_MODULE_BASIC = null;
    public static final ContainerType<ContainerModule> CONTAINER_MODULE_ACTIVATOR = null;
    public static final ContainerType<ContainerModule> CONTAINER_MODULE_DETECTOR = null;
    public static final ContainerType<ContainerModule> CONTAINER_MODULE_DISTRIBUTOR = null;
    public static final ContainerType<ContainerExtruder2Module> CONTAINER_MODULE_EXTRUDER2 = null;
    public static final ContainerType<ContainerModule> CONTAINER_MODULE_FLINGER = null;
    public static final ContainerType<ContainerModule> CONTAINER_MODULE_FLUID = null;
    public static final ContainerType<ContainerModule> CONTAINER_MODULE_PLAYER = null;
    public static final ContainerType<ContainerModule> CONTAINER_MODULE_VACUUM = null;

    public static final ContainerType<ContainerBulkItemFilter> CONTAINER_BULK_ITEM_FILTER = null;
    public static final ContainerType<ContainerModFilter> CONTAINER_MOD_FILTER = null;

    @Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
            event.getRegistry().registerAll(
                    IForgeContainerType.create(ContainerModule::new)
                            .setRegistryName(RL("container_module_basic")),
                    IForgeContainerType.create(ContainerModules::createActivatorContainer)
                            .setRegistryName(RL("container_module_activator")),
                    IForgeContainerType.create(ContainerModules::createDetectorContainer)
                            .setRegistryName(RL("container_module_detector")),
                    IForgeContainerType.create(ContainerModules::createDistributorContainer)
                            .setRegistryName(RL("container_module_distributor")),
                    IForgeContainerType.create(ContainerModules::createExtruder2Container).
                            setRegistryName(RL("container_module_extruder2")),
                    IForgeContainerType.create(ContainerModules::createFlingerContainer)
                            .setRegistryName(RL("container_module_flinger")),
                    IForgeContainerType.create(ContainerModules::createFluidContainer)
                            .setRegistryName(RL("container_module_fluid")),
                    IForgeContainerType.create(ContainerModules::createPlayerContainer)
                            .setRegistryName(RL("container_module_player")),
                    IForgeContainerType.create(ContainerModules::createVacuumContainer)
                            .setRegistryName(RL("container_module_vacuum")),
                    IForgeContainerType.create(ContainerItemRouter::new).setRegistryName(RL("container_item_router")),
                    IForgeContainerType.create(ContainerBulkItemFilter::new).setRegistryName(RL("container_bulk_item_filter")),
                    IForgeContainerType.create(ContainerModFilter::new).setRegistryName(RL("container_mod_filter"))
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerScreenFactories() {
        ScreenManager.registerFactory(CONTAINER_ITEM_ROUTER, GuiItemRouter::new);

        ScreenManager.registerFactory(CONTAINER_MODULE_BASIC, GuiModule::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_ACTIVATOR, GuiModuleActivator::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_DETECTOR, GuiModuleDetector::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_DISTRIBUTOR, GuiModuleDistributor::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_EXTRUDER2, GuiModuleExtruder2::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_FLINGER, GuiModuleFlinger::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_FLUID, GuiModuleFluid::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_PLAYER, GuiModulePlayer::new);
        ScreenManager.registerFactory(CONTAINER_MODULE_VACUUM, GuiModuleVacuum::new);

        ScreenManager.registerFactory(CONTAINER_BULK_ITEM_FILTER, GuiBulkItemFilter::new);
        ScreenManager.registerFactory(CONTAINER_MOD_FILTER, GuiModFilter::new);
    }
}