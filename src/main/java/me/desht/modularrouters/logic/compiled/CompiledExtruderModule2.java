package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CompiledExtruderModule2 extends CompiledExtruderModule1 {
    private final List<ItemStack> blockList;
    private final boolean mimic;

    public CompiledExtruderModule2(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        List<ItemStack> stacks = new ArrayList<>();
        mimic = getAugmentCount(ModItems.MIMIC_AUGMENT.get()) > 0;

        TemplateHandler handler = new TemplateHandler(stack, router);
        for (int i = 0; i < handler.getSlots() && stacks.size() < getRange(); i++) {
            ItemStack stack1 = handler.getStackInSlot(i);
            if (stack1.isEmpty()) {
                break;
            } else {
                for (int j = 0; j < stack1.getCount() && stacks.size() < getRange(); j++) {
                    stacks.add(ItemHandlerHelper.copyStackWithSize(stack1, 1));
                }
            }
        }
        blockList = ImmutableList.copyOf(stacks);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        boolean extend = shouldExtend(router);
        Level world = router.nonNullLevel();

        if (extend && distance < blockList.size()) {
            // try to extend
            if (!(blockList.get(distance).getItem() instanceof BlockItem)) {
                // non-block item; it's a spacer so just skip over
                router.getExtensionData().putInt(NBT_EXTRUDER_DIST + getFacing(), ++distance);
            } else {
                BlockPos placePos = router.getBlockPos().relative(getFacing(), distance + 1);
                BlockState state = ModBlocks.TEMPLATE_FRAME.get().defaultBlockState();
                if (BlockUtil.tryPlaceBlock(router, state, world, placePos)) {
                    world.getBlockEntity(placePos, ModBlockEntities.TEMPLATE_FRAME.get()).ifPresent(te -> {
                        te.setCamouflage(blockList.get(distance), getFacing(), getRouterFacing());
                        te.setExtendedMimic(mimic);
                        if (mimic) {
                            // in case we're mimicking a redstone emitter
                            world.updateNeighborsAt(placePos, state.getBlock());
                        }
                    });
                    router.playSound(null, placePos,
                            state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                            SoundSource.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                    router.getExtensionData().putInt(NBT_EXTRUDER_DIST + getFacing(), ++distance);
                    tryPushEntities(router.getLevel(), placePos, getFacing());
                    return true;
                }
            }
        } else if (!extend && distance > 0) {
            BlockPos breakPos = router.getBlockPos().relative(getFacing(), distance);
            BlockState oldState = world.getBlockState(breakPos);
            router.getExtensionData().putInt(NBT_EXTRUDER_DIST + getFacing(), --distance);
            if (okToBreak(oldState)) {
                if (oldState.getBlock() == ModBlocks.TEMPLATE_FRAME.get()) {
                    world.removeBlock(breakPos, false);
                }
                return true;
            }
        }

        return false;
    }

    private boolean okToBreak(BlockState state) {
        Block b = state.getBlock();
        return state.isAir() || b == ModBlocks.TEMPLATE_FRAME.get() || b instanceof IFluidBlock;
    }
}
