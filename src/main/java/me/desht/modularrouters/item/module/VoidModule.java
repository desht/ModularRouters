package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.logic.CompiledModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class VoidModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModule compiled) {
        ItemStack stack = router.getBufferItemStack();
        if (stack != null && compiled.getFilter().pass(stack)) {
            // bye bye items
            int toVoid = router.getItemsPerTick();
            ItemStack gone = router.getBuffer().extractItem(0, toVoid, false);
            return gone != null && gone.stackSize > 0;
        }
        return false;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.VOID),
                ModItems.blankModule, Items.LAVA_BUCKET);
    }
}
