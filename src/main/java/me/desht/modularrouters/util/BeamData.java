package me.desht.modularrouters.util;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class BeamData {
    private final ByteOffset offset;
    private final int duration;
    private final int color;
    private final ItemStack stack;
    private final boolean fade;
    private final boolean reversed;

    private int ticksLived = 0;

    public static final StreamCodec<RegistryFriendlyByteBuf, BeamData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BeamData decode(RegistryFriendlyByteBuf buf) {
            ByteOffset offset = ByteOffset.STREAM_CODEC.decode(buf);
            int duration = buf.readVarInt();
            int color = buf.readInt();
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return !stack.isEmpty() ?
                    new BeamData(offset, duration, color, stack, buf.readBoolean(), buf.readBoolean()) :
                    new BeamData(offset, duration, color, stack, false, false);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BeamData beamData) {
            ByteOffset.STREAM_CODEC.encode(buf, beamData.offset);
            buf.writeVarInt(beamData.duration);
            buf.writeInt(beamData.color);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, beamData.stack);
            if (!beamData.stack.isEmpty()) {
                buf.writeBoolean(beamData.fade);
                buf.writeBoolean(beamData.reversed);
            }
        }
    };

    public BeamData(ByteOffset offset, int duration, int color, ItemStack stack, boolean fade, boolean reversed) {
        this.offset = offset;
        this.duration = duration;
        this.color = color;
        this.stack = stack;
        this.fade = fade;
        this.reversed = reversed;
    }

    public ItemStack stack() {
        return stack;
    }

    public boolean fade() {
        return fade;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BeamData) obj;
        return Objects.equals(this.offset, that.offset) &&
                this.duration == that.duration &&
                this.color == that.color &&
                ItemStack.matches(this.stack, that.stack) &&
                this.fade == that.fade &&
                this.reversed == that.reversed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, duration, color, ItemStack.hashItemAndComponents(stack), fade, reversed);
    }

    @Override
    public String toString() {
        return "BeamData[" +
                "offset=" + offset + ", " +
                "duration=" + duration + ", " +
                "color=" + color + ", " +
                "stack=" + stack + ", " +
                "fade=" + fade + ", " +
                "reversed=" + reversed + ']';
    }

    public Vec3 getStart(Vec3 basePos) {
        return reversed ? offset.offset(basePos) : basePos;
    }

    public Vec3 getEnd(Vec3 basePos) {
        return reversed ? basePos : offset.offset(basePos);
    }

    public AABB getAABB(BlockPos basePos) {
        Vec3 vec = Vec3.atCenterOf(basePos);
        return new AABB(getStart(vec), getEnd(vec));
    }

    public float getProgress(float partialTicks) {
        return Mth.clamp((ticksLived - 1 + partialTicks) / duration, 0f, 1f);
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

    public record ByteOffset(byte x, byte y, byte z) {
        public static final StreamCodec<ByteBuf, ByteOffset> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BYTE, ByteOffset::x,
                ByteBufCodecs.BYTE, ByteOffset::y,
                ByteBufCodecs.BYTE, ByteOffset::z,
                ByteOffset::new
        );

        public Vec3 offset(Vec3 from) {
            return from.add(x, y, z);
        }
    }

    public static class Builder {
        private final BlockPos src;
        private final BlockPos dest;
        private final int duration;
        private final int color;
        private ItemStack stack = ItemStack.EMPTY;
        private boolean fade = false;
        private boolean reversed = false;

        public Builder(ModularRouterBlockEntity router, BlockPos dest, int color) {
            this(router.getBlockPos(), dest, router.getTickRate(), color);
        }

        public Builder(BlockPos src, BlockPos dest, int duration, int color) {
            this.src = src;
            this.dest = dest;
            this.duration = duration;
            this.color = color;
        }

        public Builder withItemStack(ItemStack stack) {
            this.stack = stack;
            return this;
        }

        public Builder fade(boolean fade) {
            this.fade = fade;
            return this;
        }

        public Builder reversed(boolean reversed) {
            this.reversed = reversed;
            return this;
        }

        public BeamData build() {
            ByteOffset offset = new ByteOffset(
                    (byte) Mth.clamp(dest.getX() - src.getX(), Byte.MIN_VALUE, Byte.MAX_VALUE),
                    (byte) Mth.clamp(dest.getY() - src.getY(), Byte.MIN_VALUE, Byte.MAX_VALUE),
                    (byte) Mth.clamp(dest.getZ() - src.getZ(), Byte.MIN_VALUE, Byte.MAX_VALUE)
            );
            return new BeamData(offset, duration, color, stack, fade, reversed);
        }
    }
}
