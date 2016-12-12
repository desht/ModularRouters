package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.item.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class MufflerUpgrade extends Upgrade {
    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.MUFFLER),
                " w ", "wuw", " w ",
                'u', ModItems.blankUpgrade,
                'w', Blocks.WOOL);
    }
}
