package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockTemplateFrame extends BlockCamo {
    private static final String BLOCK_NAME = "template_frame";

    public BlockTemplateFrame() {
        super(Properties.create(Material.CRAFTED_SNOW));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new TileEntityTemplateFrame();
    }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
        // drops nothing
    }
}
