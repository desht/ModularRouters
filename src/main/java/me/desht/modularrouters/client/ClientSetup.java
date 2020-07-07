package me.desht.modularrouters.client;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.client.gui.filter.GuiModFilter;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.client.model.ModelBakeEventHandler;
import me.desht.modularrouters.client.render.area.ModuleTargetRenderer;
import me.desht.modularrouters.client.render.item_beam.ItemBeamDispatcher;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModContainerTypes;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static KeyBinding keybindConfigure;
    public static KeyBinding keybindModuleInfo;

    public static void initEarly() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ModelBakeEventHandler.class);
        MinecraftForge.EVENT_BUS.register(ModuleTargetRenderer.class);
        MinecraftForge.EVENT_BUS.register(ItemBeamDispatcher.INSTANCE);
        MinecraftForge.EVENT_BUS.register(MouseOverHelp.class);
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> {
            setupRenderLayers();
            registerScreenFactories();
            registerKeyBindings();
        });
    }

    private static void registerKeyBindings() {
        keybindConfigure = new KeyBinding("key.modularrouters.configure", KeyConflictContext.GUI,
                InputMappings.getInputByCode(GLFW.GLFW_KEY_C, -1), "key.modularrouters.category");
        keybindModuleInfo = new KeyBinding("key.modularrouters.moduleInfo", KeyConflictContext.GUI,
                InputMappings.getInputByCode(GLFW.GLFW_KEY_I, -1), "key.modularrouters.category");

        ClientRegistry.registerKeyBinding(keybindConfigure);
        ClientRegistry.registerKeyBinding(keybindModuleInfo);
    }

    private static void setupRenderLayers() {
        // due to camouflage requirements, these need to render in any layer
        RenderTypeLookup.setRenderLayer(ModBlocks.ITEM_ROUTER.get(), renderType -> true);
        RenderTypeLookup.setRenderLayer(ModBlocks.TEMPLATE_FRAME.get(), renderType -> true);
    }

    private static void registerScreenFactories() {
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_ITEM_ROUTER.get(), GuiItemRouter::new);

        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_BASIC.get(), GuiModule::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_ACTIVATOR.get(), GuiModuleActivator::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_DETECTOR.get(), GuiModuleDetector::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_DISTRIBUTOR.get(), GuiModuleDistributor::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_EXTRUDER2.get(), GuiModuleExtruder2::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_FLINGER.get(), GuiModuleFlinger::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_FLUID.get(), GuiModuleFluid::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_PLAYER.get(), GuiModulePlayer::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MODULE_VACUUM.get(), GuiModuleVacuum::new);

        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_BULK_ITEM_FILTER.get(), GuiBulkItemFilter::new);
        ScreenManager.registerFactory(ModContainerTypes.CONTAINER_MOD_FILTER.get(), GuiModFilter::new);
    }
}
