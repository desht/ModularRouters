package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.render.item_beam.ItemBeam;
import me.desht.modularrouters.client.util.ClientUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * Sent by server to play an item beam between a router and another inventory
 */
public class ItemBeamMessage {
    private boolean reversed;
    private int duration;
    private BlockPos pos1;
    private BlockPos pos2;
    private ItemStack stack;
    private int color;
    private boolean itemFade;

    public ItemBeamMessage() {
    }

    public ItemBeamMessage(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        reversed = pb.readBoolean();
        pos1 = pb.readBlockPos();
        pos2 = pb.readBlockPos();
        stack = pb.readItemStack();
        color = pb.readInt();
        itemFade = pb.readBoolean();
        duration = pb.readVarInt();
    }

    /**
     * Create a new beam message
     * @param te the tile entity responsible for the rendering
     * @param other other end of the beam
     * @param reversed if true, items flow from other->te; if false, items flow from te->other
     * @param stack the item to render
     * @param color beam color
     * @param duration duration, in ticks
     */
    public ItemBeamMessage(TileEntity te, BlockPos other, boolean reversed, ItemStack stack, int color, int duration) {
        this.reversed = reversed;
        this.pos1 = te.getPos();
        this.pos2 = other;
        this.stack = stack;
        this.color = color;
        this.itemFade = false;
        this.duration = duration;
    }

    public ItemBeamMessage withFadeout() {
        this.itemFade = true;
        return this;
    }

    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeBoolean(reversed);
        pb.writeBlockPos(pos1);
        pb.writeBlockPos(pos2);
        pb.writeItemStack(stack);
        pb.writeInt(color);
        pb.writeBoolean(itemFade);
        pb.writeVarInt(duration);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> TileEntityItemRouter.getRouterAt(ClientUtil.theClientWorld(), pos1).ifPresent(te -> {
            BlockPos start = reversed ? pos2 : pos1;
            BlockPos   end = reversed ? pos1 : pos2;
            te.addItemBeam(new ItemBeam(start, end, reversed, stack, color, Math.max(5, duration + 1), itemFade));
        }));
        ctx.get().setPacketHandled(true);
    }
}
