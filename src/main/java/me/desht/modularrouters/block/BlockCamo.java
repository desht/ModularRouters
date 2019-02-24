package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.PropertyObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

//@Optional.Interface (iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public abstract class BlockCamo extends Block /*implements IFacade*/ {
    public static final PropertyObject<IBlockState> CAMOUFLAGE_STATE = new PropertyObject<>("held_state", IBlockState.class);
    public static final PropertyObject<IBlockReader> BLOCK_ACCESS = new PropertyObject<>("held_access", IBlockReader.class);
    public static final PropertyObject<BlockPos> BLOCK_POS = new PropertyObject<>("held_pos", BlockPos.class);

//    BlockCamo(Material materialIn, String blockName) {
//        super(materialIn, blockName);
//    }

    BlockCamo(Properties props) {
        super(props);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockReader world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        if (camo == null) return state;

        // todo 1.13 figure out extended states
        return state;
//        IBlockState camoState = camo.getCamouflage()/*.getActualState(world, pos)*/;
//        return ((IExtendedBlockState) state)
//                .withProperty(CAMOUFLAGE_STATE, camoState)
//                .withProperty(BLOCK_ACCESS, world)
//                .withProperty(BLOCK_POS, pos);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(IBlockState state, IBlockReader reader, BlockPos pos) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo != null ? camo.getCamouflage().getCollisionShape(reader, pos) : super.getCollisionShape(state, reader, pos);
    }

    @Override
    public VoxelShape getShape(IBlockState state, IBlockReader reader, BlockPos pos) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo != null ? camo.getCamouflage().getShape(reader, pos) : super.getShape(state, reader, pos);
    }


    // todo 1.13 (VoxelShape has multiple bounding boxes?)
//    @Override
//    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
//        ICamouflageable camo = getCamoState(worldIn, pos);
//        if (camo != null) {
//            addCollisionBoxToList(pos, entityBox, collidingBoxes, camo.getCamouflage().getCollisionShape(worldIn, pos));
//        } else {
//            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
//        }
//    }

    // todo 1.13 figure out how all this works.  camo won't be very useful till we do
//    @Override
//    public boolean doesSideBlockRendering(IBlockState state, IBlockReader world, BlockPos pos, EnumFacing face) {
//        ICamouflageable camo = getCamoState(world, pos);
//        return camo == null || camo.getCamouflage().doesSideBlockRendering(world, pos, face);
//    }
//
//    @Override
//    public boolean isSideSolid(IBlockState base_state, IBlockReader world, BlockPos pos, EnumFacing side) {
//        // ensure levers etc. can be attached to the block even though it can possibly emit redstone
//        ICamouflageable camo = getCamoState(world, pos);
//        return camo == null || camo.getCamouflage().isSideSolid(world, pos, side);
//    }
//
//    @Override
//    public int getLightOpacity(IBlockState state, IBlockReader world, BlockPos pos) {
//        ICamouflageable camo = getCamoState(world, pos);
//        return camo == null ? super.getLightOpacity(state, world, pos) : camo.getCamouflage().getLightOpacity(world, pos);
//    }
//
//    @Override
//    public int getLightValue(IBlockState state, IBlockReader world, BlockPos pos) {
//        ICamouflageable camo = getCamoState(world, pos);
//        return camo == null || !camo.extendedMimic() ? super.getLightValue(state, world, pos) : camo.getCamouflage().getLightValue(world, pos);
//    }
//
//    @Override
//    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
//        ICamouflageable camo = getCamoState(worldIn, pos);
//        return camo == null || !camo.extendedMimic() ? super.getBlockHardness(blockState, worldIn, pos) : camo.getCamouflage().getBlockHardness(worldIn, pos);
//    }
//
//    @Override
//    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
//        ICamouflageable camo = getCamoState(world, pos);
//        return camo == null || !camo.extendedMimic() ? super.getExplosionResistance(world, pos, exploder, explosion) : camo.getCamouflage().getBlock().getExplosionResistance(exploder);
//    }

    private ICamouflageable getCamoState(IBlockReader blockAccess, BlockPos pos) {
        TileEntity te = MiscUtil.getTileEntitySafely(blockAccess, pos);
        return te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null ? (ICamouflageable) te : null;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getWeakPower(blockState, blockAccess, pos, side) : camo.getCamouflage().getWeakPower(blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getWeakPower(blockState, blockAccess, pos, side) : camo.getCamouflage().getStrongPower(blockAccess, pos, side);
    }


    @Nullable
    @Override
    public ToolType getHarvestTool(IBlockState state) {
        return ToolType.PICKAXE;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }


//    @Nonnull
//    @Override
//    @Optional.Method(modid = "ctm-api")
//    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null) {
//            return ((ICamouflageable) te).getCamouflage();
//        }
//        return world.getBlockState(pos);
//    }
}
