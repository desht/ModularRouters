package me.desht.modularrouters.client;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.*;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.client.model.ModelBakeEventHandler;
import me.desht.modularrouters.client.render.area.CamoRenderer;
import me.desht.modularrouters.client.render.area.ModuleTargetRenderer;
import me.desht.modularrouters.client.render.item_beam.ItemBeamTileRenderer;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModTileEntities;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import static me.desht.modularrouters.util.MiscUtil.RL;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static KeyBinding keybindConfigure;
    public static KeyBinding keybindModuleInfo;

    public static void initEarly() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ModelBakeEventHandler.class);
        MinecraftForge.EVENT_BUS.register(ModuleTargetRenderer.class);
        MinecraftForge.EVENT_BUS.register(MouseOverHelp.class);
        MinecraftForge.EVENT_BUS.register(CamoRenderer.getInstance());
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            setupRenderLayers();
            registerScreenFactories();
            registerKeyBindings();
            registerItemModelOverrides();

            ClientRegistry.bindTileEntityRenderer(ModTileEntities.ITEM_ROUTER.get(), ItemBeamTileRenderer::new);
        });

        FilterGuiFactory.registerGuiHandler(ModItems.INSPECTION_FILTER.get(), GuiInspectionFilter::new);
        FilterGuiFactory.registerGuiHandler(ModItems.REGEX_FILTER.get(), GuiRegexFilter::new);
    }

    private static void registerItemModelOverrides() {
        ItemModelsProperties.register(ModItems.DISTRIBUTOR_MODULE.get(), RL("mode"), (stack, world, entity) -> {
            if (entity != null) {
                CompoundNBT compound = stack.getTagElement(ModularRouters.MODID);
                if (compound != null) {
                    return compound.getBoolean(CompiledDistributorModule.NBT_PULLING) ? 1f : 0f;
                }
            }
            return 0f;
        });
    }

    private static void registerKeyBindings() {
        keybindConfigure = new KeyBinding("key.modularrouters.configure", KeyConflictContext.GUI,
                InputMappings.getKey(GLFW.GLFW_KEY_C, -1), "key.modularrouters.category");
        keybindModuleInfo = new KeyBinding("key.modularrouters.moduleInfo", KeyConflictContext.GUI,
                InputMappings.getKey(GLFW.GLFW_KEY_I, -1), "key.modularrouters.category");

        ClientRegistry.registerKeyBinding(keybindConfigure);
        ClientRegistry.registerKeyBinding(keybindModuleInfo);
    }

    private static void setupRenderLayers() {
        // due to camouflage requirements, these need to render in any layer
        RenderTypeLookup.setRenderLayer(ModBlocks.ITEM_ROUTER.get(), renderType -> true);
        RenderTypeLookup.setRenderLayer(ModBlocks.TEMPLATE_FRAME.get(), renderType -> true);
    }

    private static void registerScreenFactories() {
        ScreenManager.register(ModContainerTypes.CONTAINER_ITEM_ROUTER.get(), GuiItemRouter::new);

        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_BASIC.get(), GuiModule::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_ACTIVATOR.get(), GuiModuleActivator::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_DETECTOR.get(), GuiModuleDetector::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_DISTRIBUTOR.get(), GuiModuleDistributor::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_EXTRUDER2.get(), GuiModuleExtruder2::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_FLINGER.get(), GuiModuleFlinger::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_FLUID.get(), GuiModuleFluid::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_PLAYER.get(), GuiModulePlayer::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MODULE_VACUUM.get(), GuiModuleVacuum::new);

        ScreenManager.register(ModContainerTypes.CONTAINER_BULK_ITEM_FILTER.get(), GuiBulkItemFilter::new);
        ScreenManager.register(ModContainerTypes.CONTAINER_MOD_FILTER.get(), GuiModFilter::new);
    }
}
