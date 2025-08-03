package me.desht.modularrouters.network;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.network.messages.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    private static final String NETWORK_VERSION = "1.0";

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ModularRouters.MODID)
                .versioned(NETWORK_VERSION);

        // clientbound
        registrar.playToClient(GuiSyncMessage.TYPE, GuiSyncMessage.STREAM_CODEC, GuiSyncMessage::handleData);
        registrar.playToClient(ItemBeamMessage.TYPE, ItemBeamMessage.STREAM_CODEC, ItemBeamMessage::handleData);
        registrar.playToClient(PushEntityMessage.TYPE, PushEntityMessage.STREAM_CODEC, PushEntityMessage::handleData);
        registrar.playToClient(RouterUpgradesSyncMessage.TYPE, RouterUpgradesSyncMessage.STREAM_CODEC, RouterUpgradesSyncMessage::handleData);

        // serverbound
        registrar.playToServer(FilterUpdateMessage.TYPE, FilterUpdateMessage.STREAM_CODEC, FilterUpdateMessage::handleData);
        registrar.playToServer(BulkFilterUpdateMessage.TYPE, BulkFilterUpdateMessage.STREAM_CODEC, BulkFilterUpdateMessage::handleData);
        registrar.playToServer(ModuleFilterMessage.TYPE, ModuleFilterMessage.STREAM_CODEC, ModuleFilterMessage::handleData);
        registrar.playToServer(ModuleSettingsMessage.TYPE, ModuleSettingsMessage.STREAM_CODEC, ModuleSettingsMessage::handleData);
        registrar.playToServer(OpenGuiMessage.TYPE, OpenGuiMessage.STREAM_CODEC, OpenGuiMessage::handleData);
        registrar.playToServer(SyncUpgradeSettingsMessage.TYPE, SyncUpgradeSettingsMessage.STREAM_CODEC, SyncUpgradeSettingsMessage::handleData);
        registrar.playToServer(ValidateModuleMessage.TYPE, ValidateModuleMessage.STREAM_CODEC, ValidateModuleMessage::handleData);

        // bidirectional
        registrar.playBidirectional(RouterSettingsMessage.TYPE, RouterSettingsMessage.STREAM_CODEC, RouterSettingsMessage::handleData);
    }
}
