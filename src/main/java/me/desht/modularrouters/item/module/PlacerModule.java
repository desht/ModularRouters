package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlacerModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack buffer = router.getBufferItemStack();
        if (buffer != null && settings.getDirection() != Module.RelativeDirection.NONE
                && isPlaceable(buffer) && settings.getFilter().pass(buffer)) {
            BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
            World world = router.getWorld();
            IBlockState current = world.getBlockState(pos);
            if (current.getBlock().isAir(current, world, pos) || current.getBlock().isReplaceable(world, pos)) {
                ItemStack toPlace = router.getBuffer().extractItem(0, 1, false);
                ItemBlock ib = (ItemBlock) toPlace.getItem();
                IBlockState newState = ib.block.getStateFromMeta(toPlace.getItem().getMetadata(toPlace.getItemDamage()));
                if (newState.getBlock().canPlaceBlockAt(world, pos) && world.setBlockState(pos, newState)) {
                    router.getBuffer().extractItem(0, 1, true);
                    if (Config.placerParticles) {
                        world.playEvent(2001, pos, Block.getStateId(newState));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPlaceable(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

}
