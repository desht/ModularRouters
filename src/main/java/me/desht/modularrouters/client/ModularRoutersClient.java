package me.desht.modularrouters.client;

import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.*;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.client.item.DistributorModeProperty;
import me.desht.modularrouters.client.item.ModuleTintSource;
import me.desht.modularrouters.client.model.ModelBakeEventHandler;
import me.desht.modularrouters.client.render.ModRenderPipelines;
import me.desht.modularrouters.client.render.ModuleTargetRenderer;
import me.desht.modularrouters.client.render.blockentity.ModularRouterBER;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.network.messages.RouterSettingsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

@Mod(value = ModularRouters.MODID, dist = Dist.CLIENT)
public class ModularRoutersClient {
    public ModularRoutersClient(ModContainer container, IEventBus modBus) {
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::registerRenderers);
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::registerItemTintSources);
        modBus.addListener(this::registerItemModelProperties);
        modBus.addListener(this::registerBlockColorHandlers);
        modBus.addListener(this::registerRenderPipelines);
        modBus.addListener(this::registerClientNetwork);
        modBus.addListener(this::registerClientExtensions);
        modBus.addListener(KeyBindings::registerKeyBindings);
        modBus.addListener(ModelBakeEventHandler::onModelBake);
        ModuleTargetRenderer.addEventListeners();

        NeoForge.EVENT_BUS.register(MouseOverHelp.class);

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ModRenderPipelines.DEBUG_QUADS_NO_DEPTH);
        event.registerPipeline(ModRenderPipelines.LINES_NO_DEPTH);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        FilterScreenFactory.registerGuiHandler(ModItems.INSPECTION_FILTER.get(), InspectionFilterScreen::new);
        FilterScreenFactory.registerGuiHandler(ModItems.REGEX_FILTER.get(), RegexFilterScreen::new);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MODULAR_ROUTER.get(), ModularRouterBER::new);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ROUTER_MENU.get(), ModularRouterScreen::new);

        event.register(ModMenuTypes.BASE_MODULE_MENU.get(), ModuleScreen::new);
        event.register(ModMenuTypes.ACTIVATOR_MENU.get(), ActivatorModuleScreen::new);
        event.register(ModMenuTypes.BREAKER_MENU.get(), BreakerModuleScreen::new);
        event.register(ModMenuTypes.DETECTOR_MENU.get(), DetectorModuleScreen::new);
        event.register(ModMenuTypes.ENERGY_DISTRIBUTOR_MENU.get(), EnergyDistributorModuleScreen::new);
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

    private void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ModuleTintSource.ID, ModuleTintSource.CODEC);
    }

    private void registerItemModelProperties(RegisterSelectItemModelPropertyEvent event) {
        event.register(DistributorModeProperty.ID, DistributorModeProperty.TYPE);
    }

    private void registerBlockColorHandlers(RegisterColorHandlersEvent.BlockTintSources event) {
        // empty list: see COLOR_TINT_EXT
        event.register(List.of(), ModBlocks.MODULAR_ROUTER.get(), ModBlocks.TEMPLATE_FRAME.get());
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(COLOR_TINT_EXT, ModBlocks.MODULAR_ROUTER.get(), ModBlocks.TEMPLATE_FRAME.get());
    }

    public void registerClientNetwork(final RegisterClientPayloadHandlersEvent event) {
        event.register(RouterSettingsMessage.TYPE, RouterSettingsMessage::handleData);
    }

    public static final IClientBlockExtensions COLOR_TINT_EXT = new IClientBlockExtensions() {
        @Override
        public void collectDynamicTintValues(BlockState state, BlockAndTintGetter level, BlockPos pos, IntList tintValues) {
            if (level.getBlockEntity(pos) instanceof ICamouflageable c && c.getCamouflage() instanceof BlockState camoState) {
                for (var tintSource : Minecraft.getInstance().getBlockColors().getTintSources(camoState)) {
                    tintValues.add(tintSource.colorInWorld(camoState, level, pos));
                }
            }
        }
    };
}
