package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.ICamouflageable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

//@Optional.Interface (iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public abstract class BlockCamo extends Block /*implements IFacade*/ {
    public static final ModelProperty<BlockState> CAMOUFLAGE_STATE = new ModelProperty<>();
    private static final VoxelShape ALMOST_FULL = makeCuboidShape(0.1, 0.1, 0.1, 15.99, 15.99, 15.99);

    BlockCamo(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedCollisionShape(state, reader, pos, ctx) : camo.getCamouflage().getCollisionShape(reader, pos, ctx);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedShape(state, reader, pos, ctx) : camo.getCamouflage().getShape(reader, pos, ctx);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || !camo.extendedMimic() || isBlacklisted(camo.getCamouflage()) ? super.getLightValue(state, world, pos) : camo.getCamouflage().getLightValue(world, pos);
    }

    @Override
    public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRaytraceShape(state, worldIn, pos) : camo.getCamouflage().getRaytraceShape(worldIn, pos, ISelectionContext.dummy());
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRenderShape(state, worldIn, pos) : camo.getCamouflage().getRenderShape(worldIn, pos);
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null ? super.getOpacity(state, world, pos) : camo.getCamouflage().getOpacity(world, pos);
    }

    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo == null || !camo.extendedMimic() ? super.getPlayerRelativeBlockHardness(state, player, worldIn, pos) : camo.getCamouflage().getPlayerRelativeBlockHardness(player, worldIn, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || !camo.extendedMimic() ?
                super.getExplosionResistance(state, world, pos, explosion) :
                camo.getCamouflage().getBlock().getExplosionResistance(state, world, pos, explosion);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo == null ? super.propagatesSkylightDown(state, reader, pos) : camo.getCamouflage().propagatesSkylightDown(reader, pos);
    }

    ICamouflageable getCamoState(IBlockReader blockAccess, BlockPos pos) {
        if (blockAccess == null || pos == null) return null;
        TileEntity te = blockAccess.getTileEntity(pos);
        return te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null ? (ICamouflageable) te : null;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getWeakPower(blockState, blockAccess, pos, side) : camo.getCamouflage().getWeakPower(blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getWeakPower(blockState, blockAccess, pos, side) : camo.getCamouflage().getStrongPower(blockAccess, pos, side);
    }

    @Nullable
    @Override
    public ToolType getHarvestTool(BlockState state) {
        return ToolType.PICKAXE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    public abstract VoxelShape getUncamouflagedShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx);

    protected VoxelShape getUncamouflagedCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx) {
        return getUncamouflagedShape(state, reader, pos, ctx);
    }

    protected VoxelShape getUncamouflagedRenderShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return getUncamouflagedShape(state, reader, pos, ISelectionContext.dummy());
    }

    protected VoxelShape getUncamouflagedRaytraceShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return VoxelShapes.empty();
    }

    private boolean isBlacklisted(BlockState camouflage) {
        // C&B chiseled blocks also have some camo functionality, and this can cause a recursive loop
        // https://github.com/desht/ModularRouters/issues/116
        return camouflage.getBlock().getRegistryName().getNamespace().equals("chiselsandbits");
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
