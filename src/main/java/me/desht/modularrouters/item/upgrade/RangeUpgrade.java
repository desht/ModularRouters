package me.desht.modularrouters.item.upgrade;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RangeUpgrade extends Upgrade {
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        list.add(I18n.format("itemText.misc.rangeUpgradeTooltip"));
        list.addAll(Arrays.asList("sender1Module", "sender2Module", "vacuumModule").stream().map(m -> "\u2022 " + I18n.format("item." + m + ".name")).collect(Collectors.toList()));
    }
}
