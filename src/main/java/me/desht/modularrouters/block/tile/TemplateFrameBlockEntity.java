package me.desht.modularrouters.block.tile;

import me.desht.modularrouters.block.BlockCamo;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.util.Scheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemplateFrameBlockEntity extends BlockEntity implements ICamouflageable {
    private static final String NBT_CAMO_NAME = "CamouflageName";
    private static final String NBT_MIMIC = "Mimic";

    private BlockState camouflage = null;  // block to masquerade as
    private boolean extendedMimic; // true if extra mimicking is done (light, hardness, blast resistance)

    public TemplateFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEMPLATE_FRAME.get(), pos, state);
    }

    @Override
    public BlockState getCamouflage() {
        return camouflage;
    }

    @Override
    public void setCamouflage(BlockState camouflage) {
        this.camouflage = camouflage;
        requestModelDataUpdate();
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
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        camouflage = getCamoStateFromNBT(compound);
        extendedMimic = compound.getBoolean(NBT_MIMIC);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound = super.save(compound);
        compound.putBoolean(NBT_MIMIC, extendedMimic);
        return getNBTFromCamoState(compound, camouflage);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        camouflage = getCamoStateFromNBT(pkt.getTag());
        extendedMimic = pkt.getTag().getBoolean("Mimic");
        if (camouflage != null && extendedMimic && camouflage.getLightEmission() > 0) {
            getLevel().getChunkSource().getLightEngine().checkBlock(worldPosition);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, -1, getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = new CompoundTag();

        compound.putInt("x", worldPosition.getX());
        compound.putInt("y", worldPosition.getY());
        compound.putInt("z", worldPosition.getZ());
        compound.putBoolean("Mimic", extendedMimic);

        return getNBTFromCamoState(compound, camouflage);
    }

    private static BlockState getCamoStateFromNBT(CompoundTag tag) {
        if (tag.contains(NBT_CAMO_NAME)) {
            return NbtUtils.readBlockState(tag.getCompound(NBT_CAMO_NAME));
        }
        return null;
    }

    private static CompoundTag getNBTFromCamoState(CompoundTag compound, BlockState camouflage) {
        if (camouflage != null) {
            compound.put(NBT_CAMO_NAME, NbtUtils.writeBlockState(camouflage));
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
        }
    }
}
