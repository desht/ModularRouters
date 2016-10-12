package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModSorterModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ModSorterModule extends SorterModule {
    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.MODSORTER),
                ItemModule.makeItemStack(ItemModule.ModuleType.SORTER), Items.GOLD_INGOT);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledModSorterModule(router, stack);
    }
}
