package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class PullerModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModule compiled) {
        ItemStack bufferStack = router.getBufferItemStack();
        if (compiled.getDirection() != Module.RelativeDirection.NONE
                && (bufferStack == null || bufferStack.stackSize < bufferStack.getMaxStackSize())) {
            IItemHandler handler = InventoryUtils.getInventory(router.getWorld(), compiled.getTarget().pos, compiled.getTarget().face);
            if (handler != null) {
                int taken = InventoryUtils.extractItems(handler, compiled, router);
                return taken > 0;
            }
        }
        return false;
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.PULLER),
                ModItems.blankModule, Blocks.STICKY_PISTON);
    }
}
