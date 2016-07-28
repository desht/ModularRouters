package me.desht.modularrouters.item.upgrade;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class RangeUpgrade extends Upgrade {
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        list.add(I18n.format("itemText.misc.rangeUpgradeTooltip"));
        for (String m : Arrays.asList("senderModule1", "senderModule2", "vacuumModule")) {
            list.add("\u2022 " + I18n.format("item." + m + ".name"));
        }
    }
}
