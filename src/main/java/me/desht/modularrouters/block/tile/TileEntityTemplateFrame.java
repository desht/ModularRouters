package me.desht.modularrouters.block.tile;

import me.desht.modularrouters.block.BlockCamo;
import me.desht.modularrouters.core.ModTileEntities;
import me.desht.modularrouters.util.Scheduler;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityTemplateFrame extends TileEntity implements ICamouflageable {
    private static final String NBT_CAMO_NAME = "CamouflageName";
    private static final String NBT_MIMIC = "Mimic";

    private BlockState camouflage = null;  // block to masquerade as
    private boolean extendedMimic; // true if extra mimicking is done (light, hardness, blast resistance)

    public TileEntityTemplateFrame() {
        super(ModTileEntities.TEMPLATE_FRAME.get());
    }

    public static Optional<TileEntityTemplateFrame> getTemplateFrame(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        return te instanceof TileEntityTemplateFrame ? Optional.of((TileEntityTemplateFrame) te) : Optional.empty();
    }

    @Override
    public BlockState getCamouflage() {
        return camouflage;
    }

    @Override
    public void setCamouflage(BlockState camouflage) {
        this.camouflage = camouflage;
        requestModelDataUpdate();
        setChanged();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(BlockCamo.CAMOUFLAGE_STATE, camouflage)
                .build();
    }

    @Override
    public boolean extendedMimic() {
        return extendedMimic;
    }

    @Override
    public void setExtendedMimic(boolean mimic) {
        this.extendedMimic = mimic;
        setChanged();
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        camouflage = getCamoStateFromNBT(compound);
        extendedMimic = compound.getBoolean(NBT_MIMIC);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound = super.save(compound);
        compound.putBoolean(NBT_MIMIC, extendedMimic);
        return getNBTFromCamoState(compound, camouflage);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        camouflage = getCamoStateFromNBT(pkt.getTag());
        extendedMimic = pkt.getTag().getBoolean("Mimic");
        if (camouflage != null && extendedMimic && camouflage.getLightEmission() > 0) {
            getLevel().getChunkSource().getLightEngine().checkBlock(worldPosition);
        }
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);

        camouflage = getCamoStateFromNBT(tag);
        extendedMimic = tag.getBoolean("Mimic");
        if (camouflage != null && extendedMimic && camouflage.getLightEmission() > 0) {
            // this needs to be deferred a tick because the chunk isn't fully loaded,
            // so any attempt to relight will be ignored
            Scheduler.client().schedule(() -> getLevel().getChunkSource().getLightEngine().checkBlock(worldPosition), 1L);
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, -1, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = new CompoundNBT();

        compound.putInt("x", worldPosition.getX());
        compound.putInt("y", worldPosition.getY());
        compound.putInt("z", worldPosition.getZ());
        compound.putBoolean("Mimic", extendedMimic);

        return getNBTFromCamoState(compound, camouflage);
    }

    private static BlockState getCamoStateFromNBT(CompoundNBT tag) {
        if (tag.contains(NBT_CAMO_NAME)) {
            return NBTUtil.readBlockState(tag.getCompound(NBT_CAMO_NAME));
        }
        return null;
    }

    private static CompoundNBT getNBTFromCamoState(CompoundNBT compound, BlockState camouflage) {
        if (camouflage != null) {
            compound.put(NBT_CAMO_NAME, NBTUtil.writeBlockState(camouflage));
        }
        return compound;
    }

    public void setCamouflage(ItemStack itemStack, Direction facing, Direction routerFacing) {
        if (itemStack.getItem() instanceof BlockItem) {
            camouflage = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
            if (camouflage.hasProperty(BlockStateProperties.AXIS)) {
                camouflage = camouflage.setValue(BlockStateProperties.AXIS, facing.getAxis());
            } else if (camouflage.hasProperty(BlockStateProperties.FACING)) {
                camouflage = camouflage.setValue(BlockStateProperties.FACING, facing);
            } else if (camouflage.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                camouflage = camouflage.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        facing.getAxis() == Direction.Axis.Y ? routerFacing : facing);
            }
            setChanged();
        }
    }
}
