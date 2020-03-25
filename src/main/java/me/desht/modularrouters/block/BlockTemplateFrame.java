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
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockTemplateFrame extends BlockCamo {
    public BlockTemplateFrame() {
        super(Properties.create(Material.GLASS));
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
        return stack.setDisplayName(stack.getDisplayName().appendText("..?"));
    }
}
