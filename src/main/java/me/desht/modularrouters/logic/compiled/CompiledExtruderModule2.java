package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.Extruder2ModuleMenu.TemplateHandler;
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
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CompiledExtruderModule2 extends CompiledExtruderModule1 {
    private final List<ItemStack> blockTemplate;
    private final boolean mimic;

    public CompiledExtruderModule2(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        mimic = getAugmentCount(ModItems.MIMIC_AUGMENT) > 0;
        blockTemplate = new TemplateHandler(stack, router).toTemplate(getRange());
    }

    @Override
    public boolean execute(ModularRouterBlockEntity router) {
        boolean extend = shouldExtend(router);
        Level world = router.nonNullLevel();

        if (extend && distance < blockTemplate.size()) {
            // try to extend
            if (!(blockTemplate.get(distance).getItem() instanceof BlockItem)) {
                // non-block item; it's a spacer so just skip over
                router.getExtensionData().putInt(NBT_EXTRUDER_DIST + getAbsoluteFacing(), ++distance);
            } else {
                assert getAbsoluteFacing() != null && getRouterFacing() != null;
                BlockPos placePos = router.getBlockPos().relative(getAbsoluteFacing(), distance + 1);
                BlockState state = ModBlocks.TEMPLATE_FRAME.get().defaultBlockState();
                if (BlockUtil.tryPlaceBlock(router, state, world, placePos)) {
                    world.getBlockEntity(placePos, ModBlockEntities.TEMPLATE_FRAME.get()).ifPresent(te -> {
                        te.setCamouflage(blockTemplate.get(distance), getAbsoluteFacing(), getRouterFacing());
                        te.setExtendedMimic(mimic);
                        if (mimic) {
                            // in case we're mimicking a redstone emitter
                            world.updateNeighborsAt(placePos, state.getBlock());
                        }
                    });
                    router.playSound(null, placePos,
                            state.getBlock().getSoundType(state, world, placePos, null).getPlaceSound(),
                            SoundSource.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                    router.getExtensionData().putInt(NBT_EXTRUDER_DIST + getAbsoluteFacing(), ++distance);
                    tryPushEntities(router.getLevel(), placePos, getAbsoluteFacing());
                    return true;
                }
            }
        } else if (!extend && distance > 0) {
            BlockPos breakPos = router.getBlockPos().relative(getAbsoluteFacing(), distance);
            BlockState oldState = world.getBlockState(breakPos);
            router.getExtensionData().putInt(NBT_EXTRUDER_DIST + getAbsoluteFacing(), --distance);
            if (okToBreak(oldState)) {
                if (oldState.getBlock() == ModBlocks.TEMPLATE_FRAME.get()) {
                    world.removeBlock(breakPos, false);
                    router.playSound(null, breakPos,
                            oldState.getBlock().getSoundType(oldState, world, breakPos, null).getPlaceSound(),
                            SoundSource.BLOCKS, 1.0f, 0.5f + distance * 0.1f);
                }
                return true;
            }
        }

        return false;
    }

    private boolean okToBreak(BlockState state) {
        Block b = state.getBlock();
        return state.isAir() || b == ModBlocks.TEMPLATE_FRAME.get() || b instanceof LiquidBlock;
    }
}
