package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class Upgrade {
    @SideOnly(Side.CLIENT)
    public abstract void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4);

    boolean hasExtraInformation() {
        return false;
    }

    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
    }

    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // no-op by default
    }

}
