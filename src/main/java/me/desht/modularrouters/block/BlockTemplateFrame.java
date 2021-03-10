package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock.Properties;

public class BlockTemplateFrame extends BlockCamo {
    public BlockTemplateFrame() {
        super(Properties.of(Material.GLASS).isValidSpawn((state, world, pos, entityType) -> false));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityTemplateFrame();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ICamouflageable camo = getCamoState(world, pos);
        if (camo == null) return super.getPickBlock(state, target, world, pos, player);
        ItemStack stack = new ItemStack(camo.getCamouflage().getBlock().asItem());
        return stack.setHoverName(stack.getHoverName().plainCopy().append("..?"));
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return VoxelShapes.block();
    }
}
