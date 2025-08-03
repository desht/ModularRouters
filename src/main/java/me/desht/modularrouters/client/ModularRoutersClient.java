package me.desht.modularrouters.client;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.client.gui.MouseOverHelp;
import me.desht.modularrouters.client.gui.filter.*;
import me.desht.modularrouters.client.gui.module.*;
import me.desht.modularrouters.client.item.DistributorModeProperty;
import me.desht.modularrouters.client.item.ModuleTintSource;
import me.desht.modularrouters.client.render.ModRenderPipelines;
import me.desht.modularrouters.client.render.area.ModuleTargetRenderer;
import me.desht.modularrouters.client.render.blockentity.ModularRouterBER;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

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
        modBus.addListener(KeyBindings::registerKeyBindings);
//        modBus.addListener(ModelBakeEventHandler::onModelBake);

        NeoForge.EVENT_BUS.register(ModuleTargetRenderer.class);
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

    private void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register((state, reader, pos, tintIndex) -> {
            if (pos == null || reader == null) return -1;
            BlockEntity te = reader.getBlockEntity(pos);
            if (te instanceof ICamouflageable camouflageable && camouflageable.getCamouflage() != null) {
                return event.getBlockColors().getColor(camouflageable.getCamouflage(), te.getLevel(), pos, tintIndex);
            } else {
                return 0xffffff;
            }
        }, ModBlocks.MODULAR_ROUTER.get(), ModBlocks.TEMPLATE_FRAME.get());
    }
}
