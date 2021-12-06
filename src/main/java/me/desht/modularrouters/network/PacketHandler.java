package me.desht.modularrouters.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
        register(RouterSettingsMessage.class,
                RouterSettingsMessage::toBytes, RouterSettingsMessage::new, RouterSettingsMessage::handle,
                null);  // bidirectional
        register(ItemBeamMessage.class,
                ItemBeamMessage::toBytes, ItemBeamMessage::new, ItemBeamMessage::handle,
                NetworkDirection.PLAY_TO_CLIENT);
        register(ModuleSettingsMessage.class,
                ModuleSettingsMessage::toBytes, ModuleSettingsMessage::new, ModuleSettingsMessage::handle,
                NetworkDirection.PLAY_TO_SERVER);
        register(FilterSettingsMessage.class,
                FilterSettingsMessage::toBytes, FilterSettingsMessage::new, FilterSettingsMessage::handle,
                NetworkDirection.PLAY_TO_SERVER);
        register(OpenGuiMessage.class,
                OpenGuiMessage::toBytes, OpenGuiMessage::new, OpenGuiMessage::handle,
                NetworkDirection.PLAY_TO_SERVER);
        register(GuiSyncMessage.class,
                GuiSyncMessage::toBytes, GuiSyncMessage::new, GuiSyncMessage::handle,
                NetworkDirection.PLAY_TO_CLIENT);
        register(SyncUpgradeSettingsMessage.class,
                SyncUpgradeSettingsMessage::toBytes, SyncUpgradeSettingsMessage::new, SyncUpgradeSettingsMessage::handle,
                NetworkDirection.PLAY_TO_SERVER);
        register(PushEntityMessage.class,
                PushEntityMessage::toBytes, PushEntityMessage::new, PushEntityMessage::handle,
                NetworkDirection.PLAY_TO_CLIENT);
        register(RouterUpgradesSyncMessage.class,
                RouterUpgradesSyncMessage::toBytes, RouterUpgradesSyncMessage::new, RouterUpgradesSyncMessage::handle,
                NetworkDirection.PLAY_TO_CLIENT);
        register(ValidateModuleMessage.class,
                ValidateModuleMessage::toBytes, ValidateModuleMessage::new, ValidateModuleMessage::handle,
                NetworkDirection.PLAY_TO_SERVER);
        register(ModuleFilterMessage.class,
                ModuleFilterMessage::toBytes, ModuleFilterMessage::new, ModuleFilterMessage::handle,
                NetworkDirection.PLAY_TO_SERVER);
    }

    private static <MSG> void register(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer, NetworkDirection direction) {
        NETWORK.registerMessage(nextId(), messageType, encoder, decoder, messageConsumer, Optional.ofNullable(direction));
    }
}
