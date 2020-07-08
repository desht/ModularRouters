package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CompiledExtruderModule2 extends CompiledExtruderModule1 {
    private final List<ItemStack> blockList;
    private final boolean mimic;

    public CompiledExtruderModule2(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        blockList = new ArrayList<>();
        mimic = getAugmentCount(ModItems.MIMIC_AUGMENT.get()) > 0;

        TemplateHandler handler = new TemplateHandler(stack, router);
        for (int i = 0; i < handler.getSlots() && blockList.size() < getRange(); i++) {
            ItemStack stack1 = handler.getStackInSlot(i);
            if (stack1.isEmpty()) {
                break;
            } else {
                for (int j = 0; j < stack1.getCount() && blockList.size() < getRange(); j++) {
                    blockList.add(ItemHandlerHelper.copyStackWithSize(stack1, 1));
                }
            }
        }
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        boolean extend = shouldExtend(router);
        World world = router.getWorld();

        if (extend && distance < blockList.size()) {
            // try to extend
            if (!(blockList.get(distance).getItem() instanceof BlockItem)) {
                // non-block item; it's a spacer so just skip over
                router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), ++distance);
            } else {
                BlockPos placePos = router.getPos().offset(getFacing(), distance + 1);
                BlockState state = ModBlocks.TEMPLATE_FRAME.get().getDefaultState();
                if (BlockUtil.tryPlaceBlock(state, world, placePos)) {
                    TileEntityTemplateFrame.getTemplateFrame(world, placePos).ifPresent(te -> {
                        te.setCamouflage(blockList.get(distance), getFacing(), getRouterFacing());
                        te.setExtendedMimic(mimic);
                        if (mimic) {
                            // in case we're mimicking a redstone emitter
                            world.notifyNeighborsOfStateChange(placePos, state.getBlock());
                        }
                    });
                    router.playSound(null, placePos,
                            state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                            SoundCategory.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                    router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), ++distance);
                    tryPushEntities(router.getWorld(), placePos, getFacing());
                    return true;
                }
            }
        } else if (!extend && distance > 0) {
            BlockPos breakPos = router.getPos().offset(getFacing(), distance);
            BlockState oldState = world.getBlockState(breakPos);
            router.getExtData().putInt(NBT_EXTRUDER_DIST + getFacing(), --distance);
            if (okToBreak(oldState, world, breakPos)) {
                if (oldState.getBlock() == ModBlocks.TEMPLATE_FRAME.get()) {
                    world.removeBlock(breakPos, false);
                }
                return true;
            }
        }

        return false;
    }

    private boolean okToBreak(BlockState state, World world, BlockPos pos) {
        Block b = state.getBlock();
        return b.isAir(state, world, pos) || b == ModBlocks.TEMPLATE_FRAME.get() || b instanceof IFluidBlock;
    }
}
