package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class PlaySoundMessage implements IMessage {
    private static Field soundNameField;
    private ResourceLocation soundName;
    private float volume;
    private float pitch;

    public static void playSound(EntityPlayer player, SoundEvent soundEvent, float volume, float pitch) {
        if (player instanceof EntityPlayerMP) {
            ModularRouters.network.sendTo(new PlaySoundMessage(soundEvent, volume, pitch), (EntityPlayerMP) player);
        }
    }

    public PlaySoundMessage() {
    }

    public PlaySoundMessage(SoundEvent soundEvent, float volume, float pitch) {
        if (soundNameField == null) {
            soundNameField = ReflectionHelper.findField(SoundEvent.class, "soundName", "field_187506_b", "b");
        }

        try {
            this.soundName = (ResourceLocation) soundNameField.get(soundEvent);
        } catch (IllegalAccessException e) {
            this.soundName = new ResourceLocation("");
        }
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        soundName = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        volume = buf.readFloat();
        pitch = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, soundName.toString());
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
    }

    public static class Handler implements IMessageHandler<PlaySoundMessage, IMessage> {
        @Override
        public IMessage onMessage(PlaySoundMessage message, MessageContext ctx) {
            EntityPlayer player = ModularRouters.proxy.getClientPlayer();
            if (player != null) {
                player.playSound(new SoundEvent(message.soundName), message.volume, message.pitch);
            }
            return null;
        }
    }
}
