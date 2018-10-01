package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.PropertyObject;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Optional;
import team.chisel.ctm.api.IFacade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface (iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public abstract class BlockCamo extends BlockBase implements IFacade {
    public static final PropertyObject<IBlockState> CAMOUFLAGE_STATE = new PropertyObject<>("held_state", IBlockState.class);
    public static final PropertyObject<IBlockAccess> BLOCK_ACCESS = new PropertyObject<>("held_access", IBlockAccess.class);
    public static final PropertyObject<BlockPos> BLOCK_POS = new PropertyObject<>("held_pos", BlockPos.class);

    BlockCamo(Material materialIn, String blockName) {
        super(materialIn, blockName);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new IProperty[] { },
                new IUnlistedProperty[] { CAMOUFLAGE_STATE, BLOCK_ACCESS, BLOCK_POS });
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        if (camo == null) return state;

        IBlockState camoState = camo.getCamouflage()/*.getActualState(world, pos)*/;
        return ((IExtendedBlockState) state)
                .withProperty(CAMOUFLAGE_STATE, camoState)
                .withProperty(BLOCK_ACCESS, world)
                .withProperty(BLOCK_POS, pos);
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        ICamouflageable camo = getCamoState(source, pos);
        return camo != null ? camo.getCamouflage().getBoundingBox(source, pos) : super.getBoundingBox(state, source, pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo != null ? camo.getCamouflage().getCollisionBoundingBox(worldIn, pos) : super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        if (camo != null) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, camo.getCamouflage().getBoundingBox(worldIn, pos));
        } else {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_);
        }
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || camo.getCamouflage().doesSideBlockRendering(world, pos, face);
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        // ensure levers etc. can be attached to the block even though it can possibly emit redstone
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || camo.getCamouflage().isSideSolid(world, pos, side);
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null ? super.getLightOpacity(state, world, pos) : camo.getCamouflage().getLightOpacity(world, pos);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || !camo.extendedMimic() ? super.getLightValue(state, world, pos) : camo.getCamouflage().getLightValue(world, pos);
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo == null || !camo.extendedMimic() ? super.getBlockHardness(blockState, worldIn, pos) : camo.getCamouflage().getBlockHardness(worldIn, pos);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || !camo.extendedMimic() ? super.getExplosionResistance(world, pos, exploder, explosion) : camo.getCamouflage().getBlock().getExplosionResistance(exploder);
    }

    private ICamouflageable getCamoState(IBlockAccess blockAccess, BlockPos pos) {
        TileEntity te = MiscUtil.getTileEntitySafely(blockAccess, pos);
        return te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null ? (ICamouflageable) te : null;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getWeakPower(blockState, blockAccess, pos, side) : camo.getCamouflage().getWeakPower(blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getWeakPower(blockState, blockAccess, pos, side) : camo.getCamouflage().getStrongPower(blockAccess, pos, side);
    }

    @Nullable
    @Override
    public String getHarvestTool(IBlockState state) {
        return "pickaxe";
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }


    @Nonnull
    @Override
    @Optional.Method(modid = "ctm-api")
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null) {
            return ((ICamouflageable) te).getCamouflage();
        }
        return world.getBlockState(pos);
    }
}
