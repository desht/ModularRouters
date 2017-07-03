package me.desht.modularrouters.client;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.Upgrade;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ItemColours {
    public static class ModuleColour implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            switch (tintIndex) {
                case 0: case 2: return Color.WHITE.getRGB();
                case 1:
                    Module m = ItemModule.getModule(stack);
                    return m == null ? Color.WHITE.getRGB() : m.getItemTint().getRGB();
                default:
                    // should never get here
                    return Color.BLACK.getRGB();
            }
        }
    }

    public static class UpgradeColour implements IItemColor {
        @Override
        public int getColorFromItemstack(ItemStack stack, int tintIndex) {
            switch (tintIndex) {
                case 0: case 2: return Color.WHITE.getRGB();
                case 1:
                    Upgrade u = ItemUpgrade.getUpgrade(stack);
                    return u == null ? Color.WHITE.getRGB() : u.getItemTint().getRGB();
                default:
                    // should never get here
                    return Color.BLACK.getRGB();
            }
        }
    }
}
