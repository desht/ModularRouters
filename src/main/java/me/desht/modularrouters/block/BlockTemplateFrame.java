package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import me.desht.modularrouters.util.PropertyObject;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockTemplateFrame extends BlockBase {
    public static final String BLOCK_NAME = "template_frame";
    public static final PropertyObject<IBlockState> CAMOUFLAGE_STATE = new PropertyObject<>("held_state", IBlockState.class);

    public BlockTemplateFrame() {
        super(Material.CIRCUITS, BLOCK_NAME);
        setDefaultState(((IExtendedBlockState) blockState.getBaseState())
                .withProperty(CAMOUFLAGE_STATE, null)
        );
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new IProperty[] { },
                new IUnlistedProperty[] { CAMOUFLAGE_STATE });
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityTemplateFrame te = TileEntityTemplateFrame.getTileEntitySafely(world, pos);
        if (te != null) {
            return ((IExtendedBlockState) state).withProperty(CAMOUFLAGE_STATE, te.getCamouflage());
        }
        return state;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState camo = getCamoState(source, pos);
        return camo != null ? camo.getBoundingBox(source, pos) : super.getBoundingBox(state, source, pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        IBlockState camo = getCamoState(worldIn, pos);
        return camo != null ? camo.getCollisionBoundingBox(worldIn, pos) : super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        IBlockState camo = getCamoState(worldIn, pos);
        if (camo != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, camo.getBoundingBox(worldIn, pos));
        } else {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState camo = getCamoState(world, pos);
        return camo == null || camo.doesSideBlockRendering(world, pos, face);
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        // ensure levers etc. can be attached to the block even though it can possibly emit redstone
        IBlockState camo = getCamoState(world, pos);
        return camo == null || camo.isSideSolid(world, pos, side);
    }

    private IBlockState getCamoState(IBlockAccess blockAccess, BlockPos pos) {
        TileEntityTemplateFrame te = TileEntityTemplateFrame.getTileEntitySafely(blockAccess, pos);
        return te != null ? te.getCamouflage() : null;
    }

//    private IBlockState getCamoState(IBlockState state) {
//        return state instanceof IExtendedBlockState ?
//                ((IExtendedBlockState) state).getValue(CAMOUFLAGE_STATE) :
//                null;
//    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityTemplateFrame();
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return Collections.emptyList();
    }
}
