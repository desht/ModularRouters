package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSorterModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.util.List;

public class SorterModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSorterModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        // deprecated in favour of bulk item filters
        return null;
//        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SORTER),
//                ItemModule.makeItemStack(ItemModule.ModuleType.DETECTOR), ItemModule.makeItemStack(ItemModule.ModuleType.SENDER1));
    }

//    @Override
//    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
//        super.addBasicInformation(itemstack, player, list, par4);
//        MiscUtil.appendMultiline(list, "itemText.deprecated.sorter");
//    }
}
