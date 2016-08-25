package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.fx.ParticleBeam;
import me.desht.modularrouters.client.fx.Vector3;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.awt.*;

public class ParticleBeamMessage implements IMessage {
    private int rgb;
    private boolean flat;
    private double x;
    private double y;
    private double z;
    private double x2;
    private double y2;
    private double z2;

    public ParticleBeamMessage() {
    }

    public ParticleBeamMessage(double x, double y, double z, double x2, double y2, double z2, Color color) {
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.x = x;
        this.y = y;
        this.z = z;
        this.flat = color != null;
        this.rgb = flat ? color.getRGB() : 0;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        x2 = buf.readDouble();
        y2 = buf.readDouble();
        z2 = buf.readDouble();
        flat = buf.readBoolean();
        rgb = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(x2);
        buf.writeDouble(y2);
        buf.writeDouble(z2);
        buf.writeBoolean(flat);
        buf.writeInt(rgb);
    }

    public static class Handler implements IMessageHandler<ParticleBeamMessage, IMessage> {
        @Override
        public IMessage onMessage(ParticleBeamMessage msg, MessageContext ctx) {
            World w = ModularRouters.proxy.theClientWorld();
            if (w != null) {
                ParticleBeam.doParticleBeam(w, new Vector3(msg.x, msg.y, msg.z), new Vector3(msg.x2, msg.y2, msg.z2), msg.flat ? new Color(msg.rgb) : null);
            }
            return null;
        }
    }
}
