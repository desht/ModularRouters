package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.fx.ParticleBeam;
import me.desht.modularrouters.client.fx.Vector3;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by server to play a particle beam when some modules run.
 */
public class ParticleBeamMessage {
    private int rgb;
    private float size;
    private boolean flat;
    private double x;
    private double y;
    private double z;
    private double x2;
    private double y2;
    private double z2;

    public ParticleBeamMessage() {
    }

    public ParticleBeamMessage(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        x2 = buf.readDouble();
        y2 = buf.readDouble();
        z2 = buf.readDouble();
        flat = buf.readBoolean();
        rgb = buf.readInt();
        size = buf.readByte() / 255.0f;
    }

    public ParticleBeamMessage(double x, double y, double z, double x2, double y2, double z2, Color color, float size) {
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.x = x;
        this.y = y;
        this.z = z;
        this.flat = color != null;
        this.rgb = flat ? color.getRGB() : 0;
        this.size = size;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(x2);
        buf.writeDouble(y2);
        buf.writeDouble(z2);
        buf.writeBoolean(flat);
        buf.writeInt(rgb);
        buf.writeByte((byte)(size * 255));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ModularRouters.proxy.theClientWorld();
            if (w != null) {
                ParticleBeam.doParticleBeam(w, new Vector3(x, y, z), new Vector3(x2, y2, z2),
                        flat ? new Color(rgb) : null, size);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
