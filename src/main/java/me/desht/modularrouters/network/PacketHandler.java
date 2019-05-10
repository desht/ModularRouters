package me.desht.modularrouters.network;

import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
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
        NETWORK.registerMessage(nextId(), RouterSettingsMessage.class,
                RouterSettingsMessage::toBytes, RouterSettingsMessage::new, RouterSettingsMessage::handle);
        NETWORK.registerMessage(nextId(), ParticleBeamMessage.class,
                ParticleBeamMessage::toBytes, ParticleBeamMessage::new, ParticleBeamMessage::handle);
        NETWORK.registerMessage(nextId(), ModuleSettingsMessage.class,
                ModuleSettingsMessage::toBytes, ModuleSettingsMessage::new, ModuleSettingsMessage::handle);
        NETWORK.registerMessage(nextId(), FilterSettingsMessage.class,
                FilterSettingsMessage::toBytes, FilterSettingsMessage::new, FilterSettingsMessage::handle);
        NETWORK.registerMessage(nextId(), OpenGuiMessage.class,
                OpenGuiMessage::toBytes, OpenGuiMessage::new, OpenGuiMessage::handle);
        NETWORK.registerMessage(nextId(), GuiSyncMessage.class,
                GuiSyncMessage::toBytes, GuiSyncMessage::new, GuiSyncMessage::handle);
        NETWORK.registerMessage(nextId(), SyncUpgradeSettingsMessage.class,
                SyncUpgradeSettingsMessage::toBytes, SyncUpgradeSettingsMessage::new, SyncUpgradeSettingsMessage::handle);
        NETWORK.registerMessage(nextId(), PushEntityMessage.class,
                PushEntityMessage::toBytes, PushEntityMessage::new, PushEntityMessage::handle);
        NETWORK.registerMessage(nextId(), PlaySoundMessage.class,
                PlaySoundMessage::toBytes, PlaySoundMessage::new, PlaySoundMessage::handle);
    }
}
