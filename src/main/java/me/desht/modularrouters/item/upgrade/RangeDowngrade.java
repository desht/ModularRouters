package me.desht.modularrouters.item.upgrade;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RangeDowngrade extends RangeUpgrade {
    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGEDOWN),
                ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGE));
    }
}
