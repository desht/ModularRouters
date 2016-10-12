package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ExtruderModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruderModule(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.EXTRUDER),
                ItemModule.makeItemStack(ItemModule.ModuleType.PLACER),
                Items.REDSTONE,
                ItemModule.makeItemStack(ItemModule.ModuleType.BREAKER));
    }
}
