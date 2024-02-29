package me.desht.modularrouters.network;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.network.messages.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String NETWORK_VERSION = "1.0";

    @SuppressWarnings("Convert2MethodRef")
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(ModularRouters.MODID)
                .versioned(NETWORK_VERSION);

        // clientbound
        registrar.play(GuiSyncMessage.ID, GuiSyncMessage::new, handler -> handler
                .client((message, context) -> ClientPayloadHandler.handleData(message, context)));
        registrar.play(ItemBeamMessage.ID, ItemBeamMessage::fromNetwork, handler -> handler
                .client((message, context) -> ClientPayloadHandler.handleData(message, context)));
        registrar.play(PushEntityMessage.ID, PushEntityMessage::new, handler -> handler
                .client((message, context) -> ClientPayloadHandler.handleData(message, context)));
        registrar.play(RouterUpgradesSyncMessage.ID, RouterUpgradesSyncMessage::new, handler -> handler
                .client((message, context) -> ClientPayloadHandler.handleData(message, context)));

        // serverbound
        registrar.play(FilterSettingsMessage.ID, FilterSettingsMessage::new, handler -> handler
                .server((message, context) -> ServerPayloadHandler.handleData(message, context)));
        registrar.play(ModuleFilterMessage.ID, ModuleFilterMessage::new, handler -> handler
                .server((message, context) -> ServerPayloadHandler.handleData(message, context)));
        registrar.play(ModuleSettingsMessage.ID, ModuleSettingsMessage::new, handler -> handler
                .server((message, context) -> ServerPayloadHandler.handleData(message, context)));
        registrar.play(OpenGuiMessage.ID, OpenGuiMessage::new, handler -> handler
                .server((message, context) -> ServerPayloadHandler.handleData(message, context)));
        registrar.play(SyncUpgradeSettingsMessage.ID, SyncUpgradeSettingsMessage::new, handler -> handler
                .server((message, context) -> ServerPayloadHandler.handleData(message, context)));
        registrar.play(ValidateModuleMessage.ID, ValidateModuleMessage::new, handler -> handler
                .server((message, context) -> ServerPayloadHandler.handleData(message, context)));

        // bidirectional
        registrar.play(RouterSettingsMessage.ID, RouterSettingsMessage::new, handler -> handler
                .server(NetworkHandler::handleData)
                .client(NetworkHandler::handleData)
        );
    }

    public static void handleData(RouterSettingsMessage message, PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            Level level = player.level();
            if (level.isLoaded(message.pos())) {
                level.getBlockEntity(message.pos(), ModBlockEntities.MODULAR_ROUTER.get()).ifPresent(router -> {
                    router.setRedstoneBehaviour(message.redstoneBehaviour());
                    router.setEcoMode(message.ecoMode());
                    router.setEnergyDirection(message.energyDirection());
                });
            }
        }));
    }
}
