package me.desht.modularrouters.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.*;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.client.model.ModelBakeEventHandler;
import me.desht.modularrouters.client.render.area.ModuleTargetRenderer;
import me.desht.modularrouters.client.render.blockentity.ModularRouterBER;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // these all add stuff to non-threadsafe hashmaps - defer to the main thread
            registerScreenFactories();
            registerItemModelOverrides();
        });

        FilterScreenFactory.registerGuiHandler(ModItems.INSPECTION_FILTER.get(), InspectionFilterScreen::new);
        FilterScreenFactory.registerGuiHandler(ModItems.REGEX_FILTER.get(), RegexFilterScreen::new);
    }

    @SubscribeEvent
    public static void init(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MODULAR_ROUTER.get(), ModularRouterBER::new);
    }

    @SubscribeEvent
    public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
        keybindConfigure = new KeyMapping("key.modularrouters.configure", KeyConflictContext.GUI,
                InputConstants.getKey(GLFW.GLFW_KEY_C, -1), "key.modularrouters.category");
        keybindModuleInfo = new KeyMapping("key.modularrouters.moduleInfo", KeyConflictContext.GUI,
                InputConstants.getKey(GLFW.GLFW_KEY_I, -1), "key.modularrouters.category");

        event.register(keybindConfigure);
        event.register(keybindModuleInfo);
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

    private static void registerScreenFactories() {
        MenuScreens.register(ModMenuTypes.ROUTER_MENU.get(), ModularRouterScreen::new);

        MenuScreens.register(ModMenuTypes.BASE_MODULE_MENU.get(), AbstractModuleScreen::new);
        MenuScreens.register(ModMenuTypes.ACTIVATOR_MENU.get(), ActivatorModuleScreen::new);
        MenuScreens.register(ModMenuTypes.BREAKER_MENU.get(), BreakerModuleScreen::new);
        MenuScreens.register(ModMenuTypes.DETECTOR_MENU.get(), DetectorModuleScreen::new);
        MenuScreens.register(ModMenuTypes.DISTRIBUTOR_MENU.get(), DistributorModuleScreen::new);
        MenuScreens.register(ModMenuTypes.EXTRUDER2_MENU.get(), ExtruderModule2Screen::new);
        MenuScreens.register(ModMenuTypes.FLINGER_MENU.get(), FlingerModuleScreen::new);
        MenuScreens.register(ModMenuTypes.FLUID_MENU.get(), FluidModuleScreen::new);
        MenuScreens.register(ModMenuTypes.GAS_MENU.get(), GasModuleScreen::new);
        MenuScreens.register(ModMenuTypes.PLAYER_MENU.get(), PlayerModuleScreen::new);
        MenuScreens.register(ModMenuTypes.VACUUM_MENU.get(), VacuumModuleScreen::new);

        MenuScreens.register(ModMenuTypes.BULK_FILTER_MENU.get(), BulkItemFilterScreen::new);
        MenuScreens.register(ModMenuTypes.MOD_FILTER_MENU.get(), ModFilterScreen::new);
    }
}
