package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by server to play a sound on the client.
 */
public class PlaySoundMessage {
    private static Field soundNameField;
    private ResourceLocation soundName;
    private float volume;
    private float pitch;

    public static void playSound(EntityPlayer player, SoundEvent soundEvent, float volume, float pitch) {
        if (player instanceof EntityPlayerMP) {
            PacketHandler.NETWORK.send(PacketDistributor.PLAYER.with(() -> (EntityPlayerMP) player), new PlaySoundMessage(soundEvent, volume, pitch));
        }
    }

    public PlaySoundMessage() {
    }

    private PlaySoundMessage(SoundEvent soundEvent, float volume, float pitch) {
        this.soundName = soundEvent.getRegistryName();
        this.volume = volume;
        this.pitch = pitch;
    }

    public PlaySoundMessage(ByteBuf buf) {
        soundName = new ResourceLocation(PacketUtil.readUTF8String(buf));
        volume = buf.readFloat();
        pitch = buf.readFloat();
    }

    public void toBytes(ByteBuf buffer) {
        PacketUtil.writeUTF8String(buffer, soundName.toString());
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            EntityPlayer player = ModularRouters.proxy.getClientPlayer();
            if (player != null) {
                player.playSound(ForgeRegistries.SOUND_EVENTS.getValue(soundName), volume, pitch);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
