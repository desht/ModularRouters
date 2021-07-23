package me.desht.modularrouters.block;

import me.desht.modularrouters.block.tile.ICamouflageable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public abstract class BlockCamo extends Block /*implements IFacade*/ {
    public static final ModelProperty<BlockState> CAMOUFLAGE_STATE = new ModelProperty<>();

    BlockCamo(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedCollisionShape(state, reader, pos, ctx) : camo.getCamouflage().getCollisionShape(reader, pos, ctx);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo == null ? getUncamouflagedShape(state, reader, pos, ctx) : camo.getCamouflage().getShape(reader, pos, ctx);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || !camo.extendedMimic() || isBlacklisted(camo.getCamouflage()) ? super.getLightEmission(state, world, pos) : camo.getCamouflage().getLightEmission(world, pos);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRaytraceShape(state, worldIn, pos) : camo.getCamouflage().getVisualShape(worldIn, pos, CollisionContext.empty());
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo == null ? getUncamouflagedRenderShape(state, worldIn, pos) : camo.getCamouflage().getBlockSupportShape(worldIn, pos);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null ? super.getLightBlock(state, world, pos) : camo.getCamouflage().getLightBlock(world, pos);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        ICamouflageable camo = getCamoState(worldIn, pos);
        return camo == null || !camo.extendedMimic() ? super.getDestroyProgress(state, player, worldIn, pos) : camo.getCamouflage().getDestroyProgress(player, worldIn, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        ICamouflageable camo = getCamoState(world, pos);
        return camo == null || !camo.extendedMimic() ?
                super.getExplosionResistance(state, world, pos, explosion) :
                camo.getCamouflage().getBlock().getExplosionResistance(state, world, pos, explosion);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        ICamouflageable camo = getCamoState(reader, pos);
        return camo == null ? super.propagatesSkylightDown(state, reader, pos) : camo.getCamouflage().propagatesSkylightDown(reader, pos);
    }

    ICamouflageable getCamoState(BlockGetter blockAccess, BlockPos pos) {
        if (blockAccess == null || pos == null) return null;
        BlockEntity te = blockAccess.getBlockEntity(pos);
        return te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null ? (ICamouflageable) te : null;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getSignal(blockState, blockAccess, pos, side) : camo.getCamouflage().getSignal(blockAccess, pos, side);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        ICamouflageable camo = getCamoState(blockAccess, pos);
        return camo == null || !camo.extendedMimic() ? super.getSignal(blockState, blockAccess, pos, side) : camo.getCamouflage().getDirectSignal(blockAccess, pos, side);
    }

    @Nullable
    @Override
    public ToolType getHarvestTool(BlockState state) {
        return ToolType.PICKAXE;
    }

    public abstract VoxelShape getUncamouflagedShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx);

    protected VoxelShape getUncamouflagedCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx) {
        return getUncamouflagedShape(state, reader, pos, ctx);
    }

    protected VoxelShape getUncamouflagedRenderShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return getUncamouflagedShape(state, reader, pos, CollisionContext.empty());
    }

    protected VoxelShape getUncamouflagedRaytraceShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return Shapes.empty();
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
