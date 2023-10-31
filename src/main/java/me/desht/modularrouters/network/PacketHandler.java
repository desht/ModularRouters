package me.desht.modularrouters.network;

import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.MessageFunctions;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.network.simple.SimpleMessage;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
            .named(RL("main_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    private static int det = 0;

    private static int nextId() {
        return det++;
    }

    public static void setupNetwork() {
        register(RouterSettingsMessage.class, RouterSettingsMessage::new, null);
        register(ItemBeamMessage.class, ItemBeamMessage::new, PlayNetworkDirection.PLAY_TO_CLIENT);
        register(ModuleSettingsMessage.class, ModuleSettingsMessage::new, PlayNetworkDirection.PLAY_TO_SERVER);
        register(FilterSettingsMessage.class, FilterSettingsMessage::new, PlayNetworkDirection.PLAY_TO_SERVER);
        register(OpenGuiMessage.class, OpenGuiMessage::new, PlayNetworkDirection.PLAY_TO_SERVER);
        register(GuiSyncMessage.class, GuiSyncMessage::new, PlayNetworkDirection.PLAY_TO_CLIENT);
        register(SyncUpgradeSettingsMessage.class, SyncUpgradeSettingsMessage::new, PlayNetworkDirection.PLAY_TO_SERVER);
        register(PushEntityMessage.class, PushEntityMessage::new, PlayNetworkDirection.PLAY_TO_CLIENT);
        register(RouterUpgradesSyncMessage.class, RouterUpgradesSyncMessage::new, PlayNetworkDirection.PLAY_TO_CLIENT);
        register(ValidateModuleMessage.class, ValidateModuleMessage::new, PlayNetworkDirection.PLAY_TO_SERVER);
        register(ModuleFilterMessage.class, ModuleFilterMessage::new, PlayNetworkDirection.PLAY_TO_SERVER);
    }

    private static <MSG extends SimpleMessage> void register(Class<MSG> cls, MessageFunctions.MessageDecoder<MSG> decoder, PlayNetworkDirection direction) {
        NETWORK.simpleMessageBuilder(cls, nextId(), direction).decoder(decoder).add();
    }
}
