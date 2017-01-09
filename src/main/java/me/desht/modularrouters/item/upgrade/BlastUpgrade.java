package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.item.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class BlastUpgrade extends Upgrade {
    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.BLAST),
                "ioi", "obo", "ioi",
                'i', Blocks.IRON_BARS, 'b', ModItems.blankUpgrade, 'o', Blocks.OBSIDIAN);
    }
}
