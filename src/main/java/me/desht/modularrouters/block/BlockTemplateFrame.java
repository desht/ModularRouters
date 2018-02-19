package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BlockTemplateFrame extends BlockCamo {
    private static final String BLOCK_NAME = "template_frame";

    public BlockTemplateFrame() {
        super(Material.CRAFTED_SNOW, BLOCK_NAME);
        setDefaultState(((IExtendedBlockState) blockState.getBaseState()).withProperty(CAMOUFLAGE_STATE, null));
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityTemplateFrame();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        // drops nothing
    }
}
