package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.ArrayList;
import java.util.List;

public class CompiledExtruder2Module extends CompiledExtruderModule {
    private static final ItemStack TEMPLATE_STACK = new ItemStack(ModBlocks.templateFrame);
    private final List<ItemStack> blockList;

    public CompiledExtruder2Module(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        blockList = new ArrayList<>();

        int maxDist = ExtruderModule2.maxDistance(router);
        TemplateHandler handler = new TemplateHandler(stack);
        for (int i = 0; i < handler.getSlots() && blockList.size() < maxDist; i++) {
            ItemStack stack1 = handler.getStackInSlot(i);
            for (int j = 0; j < stack1.getCount(); j++) {
                ItemStack copy = stack1.copy();
                copy.setCount(1);
                blockList.add(copy);
                if (blockList.size() >= maxDist) break;
            }
        }
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        boolean extend = shouldExtend(router);
        World world = router.getWorld();

        if (extend && distance < blockList.size()) {
            // try to extend
            BlockPos placePos = router.getPos().offset(getFacing(), distance + 1);
            IBlockState state = BlockUtil.tryPlaceAsBlock(TEMPLATE_STACK, world, placePos, getFacing());
            if (state != null) {
                TileEntityTemplateFrame te = TileEntityTemplateFrame.getTileEntitySafely(world, placePos);
                if (te != null) {
                    te.setCamouflage(blockList.get(distance));
                }
                router.playSound(null, placePos,
                        state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                        SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                router.getExtData().setInteger(NBT_EXTRUDER_DIST + getFacing(), ++distance);
                return true;
            }
        } else if (!extend && distance > 0) {
            BlockPos breakPos = router.getPos().offset(getFacing(), distance);
            IBlockState oldState = world.getBlockState(breakPos);
            if (okToBreak(oldState, world, breakPos)) {
                BlockUtil.BreakResult breakResult = BlockUtil.tryBreakBlock(world, breakPos, getFilter(), false, 0);
                router.getExtData().setInteger(NBT_EXTRUDER_DIST + getFacing(), --distance);
                if (breakResult.isBlockBroken()) {
                    router.playSound(null, breakPos,
                            ModBlocks.templateFrame.getSoundType(oldState, world, breakPos, null).getBreakSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        }

        return false;
    }

    private boolean okToBreak(IBlockState state, World world, BlockPos pos) {
        Block b = state.getBlock();
        return b.isAir(state, world, pos) || b == ModBlocks.templateFrame || b instanceof BlockLiquid || b instanceof IFluidBlock;
    }
}
