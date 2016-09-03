package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.logic.CompiledModule;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class DropperModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModule compiled) {
        ItemStack stack = router.getBufferItemStack();
        if (stack != null && compiled.getDirection() != Module.RelativeDirection.NONE && compiled.getFilter().pass(stack)) {
            int nItems = router.getItemsPerTick();
            ItemStack toDrop = router.getBuffer().extractItem(0, nItems, true);
            BlockPos pos = compiled.getTarget().pos;
            EnumFacing face = compiled.getTarget().face;
            EntityItem item = new EntityItem(router.getWorld(),
                    pos.getX() + 0.5 + 0.2 * face.getFrontOffsetX(),
                    pos.getY() + 0.5 + 0.2 * face.getFrontOffsetY(),
                    pos.getZ() + 0.5 + 0.2 * face.getFrontOffsetZ(),
                    toDrop);
            setupItemVelocity(router, item, compiled);
            if (router.getWorld().spawnEntityInWorld(item)) {
                router.getBuffer().extractItem(0, toDrop.stackSize, false);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.DROPPER), ModItems.blankModule, Blocks.DROPPER);
    }

    protected void setupItemVelocity(TileEntityItemRouter router, EntityItem item, CompiledModule settings) {
        item.motionX = item.motionY = item.motionZ = 0.0;
    }
}
