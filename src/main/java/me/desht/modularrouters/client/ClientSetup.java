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
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import static me.desht.modularrouters.util.MiscUtil.RL;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static KeyMapping keybindConfigure;
    public static KeyMapping keybindModuleInfo;

    public static void initEarly(IEventBus modBus) {
        modBus.register(ModelBakeEventHandler.class);
        NeoForge.EVENT_BUS.register(ModuleTargetRenderer.class);
        NeoForge.EVENT_BUS.register(MouseOverHelp.class);
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // non-thread-safe work here
            registerItemModelOverrides();
        });

        FilterScreenFactory.registerGuiHandler(ModItems.INSPECTION_FILTER.get(), InspectionFilterScreen::new);
        FilterScreenFactory.registerGuiHandler(ModItems.REGEX_FILTER.get(), RegexFilterScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
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

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ROUTER_MENU.get(), ModularRouterScreen::new);

        event.register(ModMenuTypes.BASE_MODULE_MENU.get(), AbstractModuleScreen::new);
        event.register(ModMenuTypes.ACTIVATOR_MENU.get(), ActivatorModuleScreen::new);
        event.register(ModMenuTypes.BREAKER_MENU.get(), BreakerModuleScreen::new);
        event.register(ModMenuTypes.DETECTOR_MENU.get(), DetectorModuleScreen::new);
        event.register(ModMenuTypes.DISTRIBUTOR_MENU.get(), DistributorModuleScreen::new);
        event.register(ModMenuTypes.EXTRUDER2_MENU.get(), ExtruderModule2Screen::new);
        event.register(ModMenuTypes.FLINGER_MENU.get(), FlingerModuleScreen::new);
        event.register(ModMenuTypes.FLUID_MENU.get(), FluidModuleScreen::new);
        event.register(ModMenuTypes.PLAYER_MENU.get(), PlayerModuleScreen::new);
        event.register(ModMenuTypes.VACUUM_MENU.get(), VacuumModuleScreen::new);

        event.register(ModMenuTypes.BULK_FILTER_MENU.get(), BulkItemFilterScreen::new);
        event.register(ModMenuTypes.MOD_FILTER_MENU.get(), ModFilterScreen::new);
        event.register(ModMenuTypes.TAG_FILTER_MENU.get(), TagFilterScreen::new);
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
}
