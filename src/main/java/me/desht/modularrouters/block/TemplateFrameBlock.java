package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.block.tile.TemplateFrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class TemplateFrameBlock extends CamouflageableBlock implements EntityBlock {
    public TemplateFrameBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ICamouflageable camo = getCamoState(level, pos);
        if (camo == null) {
            return super.getCloneItemStack(level, pos, state);
        }
        ItemStack stack = new ItemStack(camo.getCamouflage().getBlock().asItem());
        return stack.setHoverName(stack.getHoverName().plainCopy().append("..?"));
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        return Shapes.block();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TemplateFrameBlockEntity(blockPos, blockState);
    }
}
