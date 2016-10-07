package me.desht.modularrouters.item.module;

import net.minecraft.init.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ModSorterModule extends SorterModule {
    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.MODSORTER),
                ItemModule.makeItemStack(ItemModule.ModuleType.SORTER), Items.GOLD_INGOT);
    }
}
