package me.desht.modularrouters.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.*;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.client.model.ModelBakeEventHandler;
import me.desht.modularrouters.client.render.area.CamoRenderer;
import me.desht.modularrouters.client.render.area.ModuleTargetRenderer;
import me.desht.modularrouters.client.render.item_beam.ItemBeamTileRenderer;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import static me.desht.modularrouters.util.MiscUtil.RL;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static KeyMapping keybindConfigure;
    public static KeyMapping keybindModuleInfo;

    public static void initEarly() {
        FMLJavaModLoadingContext.get().getModEventBus().register(ModelBakeEventHandler.class);
        MinecraftForge.EVENT_BUS.register(ModuleTargetRenderer.class);
        MinecraftForge.EVENT_BUS.register(MouseOverHelp.class);
        MinecraftForge.EVENT_BUS.register(CamoRenderer.getInstance());
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // these all add stuff to non-threadsafe hashmaps - need to defer to the main thread
            setupRenderLayers();
            registerScreenFactories();
            registerItemModelOverrides();
        });

        registerKeyBindings();

        BlockEntityRenderers.register(ModBlockEntities.MODULAR_ROUTER.get(), ItemBeamTileRenderer::new);

        FilterScreenFactory.registerGuiHandler(ModItems.INSPECTION_FILTER.get(), InspectionFilterScreen::new);
        FilterScreenFactory.registerGuiHandler(ModItems.REGEX_FILTER.get(), RegexFilterScreen::new);
    }

    private static void registerItemModelOverrides() {
        ItemProperties.register(ModItems.DISTRIBUTOR_MODULE.get(), RL("mode"), (stack, world, entity, n) -> {
            if (entity != null) {
                CompoundTag compound = stack.getTagElement(ModularRouters.MODID);
                if (compound != null) {
                    return compound.getBoolean(CompiledDistributorModule.NBT_PULLING) ? 1f : 0f;
                }
            }
            return 0f;
        });
    }

    private static void registerKeyBindings() {
        keybindConfigure = new KeyMapping("key.modularrouters.configure", KeyConflictContext.GUI,
                InputConstants.getKey(GLFW.GLFW_KEY_C, -1), "key.modularrouters.category");
        keybindModuleInfo = new KeyMapping("key.modularrouters.moduleInfo", KeyConflictContext.GUI,
                InputConstants.getKey(GLFW.GLFW_KEY_I, -1), "key.modularrouters.category");

        ClientRegistry.registerKeyBinding(keybindConfigure);
        ClientRegistry.registerKeyBinding(keybindModuleInfo);
    }

    private static void setupRenderLayers() {
        // due to camouflage requirements, these need to render in any layer
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MODULAR_ROUTER.get(), renderType -> true);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TEMPLATE_FRAME.get(), renderType -> true);
    }

    private static void registerScreenFactories() {
        MenuScreens.register(ModContainerTypes.CONTAINER_ITEM_ROUTER.get(), ModularRouterScreen::new);

        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_BASIC.get(), AbstractModuleScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_ACTIVATOR.get(), ActivatorModuleScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_DETECTOR.get(), DetectorModuleScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_DISTRIBUTOR.get(), DistributorModuleScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_EXTRUDER2.get(), ExtruderModule2Screen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_FLINGER.get(), FlingerModuleScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_FLUID.get(), FluidModuleScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_PLAYER.get(), PlayerModuleScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MODULE_VACUUM.get(), VacuumModuleScreen::new);

        MenuScreens.register(ModContainerTypes.CONTAINER_BULK_ITEM_FILTER.get(), BulkItemFilterScreen::new);
        MenuScreens.register(ModContainerTypes.CONTAINER_MOD_FILTER.get(), ModFilterScreen::new);
    }
}
