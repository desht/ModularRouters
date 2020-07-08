package me.desht.modularrouters.client.render.item_beam;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

public class ItemBeam {
    final Vector3f startPos;
    final Vector3f endPos;
    final boolean reversed;
    final ItemStack renderItem;
    final int[] colors;
    final int lifeTime;  // ticks
    final boolean itemFade;
    int ticksLived = 0;

    public ItemBeam(BlockPos pos1, BlockPos pos2, boolean reversed, ItemStack renderItem, int color, int lifeTime, boolean itemFade) {
        this.startPos = new Vector3f(pos1.getX() + 0.5f, pos1.getY() + 0.5f, pos1.getZ() + 0.5f);
        this.endPos = new Vector3f(pos2.getX() + 0.5f, pos2.getY() + 0.5f, pos2.getZ() + 0.5f);
        this.reversed = reversed;
        this.renderItem = renderItem;
        this.colors = decompose(color);
        this.lifeTime = lifeTime;
        this.itemFade = itemFade;
    }

    private int[] decompose(int color) {
        int[] res = new int[3];
        res[0] = color >> 16 & 0xff;
        res[1] = color >> 8  & 0xff;
        res[2] = color       & 0xff;
        return res;
    }

    public AxisAlignedBB getAABB() {
        return new AxisAlignedBB(startPos.getX(), startPos.getY(), startPos.getZ(), endPos.getX(), endPos.getY(), endPos.getZ());
    }

    public void tick() {
        ticksLived++;
    }

    public boolean isExpired() {
        return ticksLived >= lifeTime;
    }
}
