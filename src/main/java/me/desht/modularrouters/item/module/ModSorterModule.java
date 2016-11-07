package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModSorterModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.util.List;

public class ModSorterModule extends SorterModule {
    @Override
    public IRecipe getRecipe() {
//        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.MODSORTER),
//                ItemModule.makeItemStack(ItemModule.ModuleType.SORTER), Items.GOLD_INGOT);
        // Mod Sorter module is deprecated - use a Mod Filter instead, it's more efficient
        return null;
    }

    @Override
    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addBasicInformation(itemstack, player, list, par4);
        list.remove(list.size() - 1); list.remove(list.size() - 1);  // remove sorter deprecation message
        MiscUtil.appendMultiline(list, "itemText.deprecated.modSorter");
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledModSorterModule(router, stack);
    }
}
