package me.desht.modularrouters.util;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class BeamData {
    private final BlockPos dest;
    private final int color;
    private final int duration;
    private final ItemStack stack;
    private boolean itemFade;
    private boolean reversed;

    private int ticksLived = 0;

    public BeamData(int duration, BlockPos dest, ItemStack stack, int color) {
        this.duration = duration;
        this.dest = dest;
        this.stack = stack;
        this.color = color;
    }

    public BeamData(int duration, BlockPos dest, int color) {
        this.duration = duration;
        this.dest = dest;
        this.stack = ItemStack.EMPTY;
        this.color = color;
    }

    public BeamData(PacketBuffer buf, BlockPos pos1) {
        byte x = buf.readByte();
        byte y = buf.readByte();
        byte z = buf.readByte();
        this.dest = pos1.offset(-x, -y, -z);
        this.color = buf.readInt();
        this.duration = buf.readVarInt();
        this.stack = buf.readItem();
        if (!stack.isEmpty()) {
            this.itemFade = buf.readBoolean();
            this.reversed = buf.readBoolean();
        }
    }

    public void toBytes(PacketBuffer buf, BlockPos pos1) {
        buf.writeByte(pos1.getX() - dest.getX());
        buf.writeByte(pos1.getY() - dest.getY());
        buf.writeByte(pos1.getZ() - dest.getZ());
        buf.writeInt(color);
        buf.writeVarInt(duration);
        buf.writeItem(stack);
        if (!stack.isEmpty()) {
            buf.writeBoolean(itemFade);
            buf.writeBoolean(reversed);
        }
    }

    public BeamData reverseItems() {
        this.reversed = true;
        return this;
    }

    public BeamData fadeItems() {
        this.itemFade = true;
        return this;
    }

    public Vector3d getStart(Vector3d basePos) {
        return reversed ? Vector3d.atCenterOf(dest) : basePos;
    }

    public Vector3d getEnd(Vector3d basePos) {
        return reversed ? basePos : Vector3d.atCenterOf(dest);
    }

    public AxisAlignedBB getAABB(BlockPos basePos) {
        return new AxisAlignedBB(basePos, dest);
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isItemFade() {
        return itemFade;
    }

    public float getProgress(float partialTicks) {
        return MathHelper.clamp((ticksLived - 1 + partialTicks) / duration, 0f, 1f);
    }

    public void tick() {
        ticksLived++;
    }

    public boolean isExpired() {
        return ticksLived > duration;
    }

    public int[] getRGB() {
        int[] res = new int[3];
        res[0] = color >> 16 & 0xff;
        res[1] = color >> 8  & 0xff;
        res[2] = color       & 0xff;
        return res;
    }

    public boolean isReversed() {
        return reversed;
    }
}
