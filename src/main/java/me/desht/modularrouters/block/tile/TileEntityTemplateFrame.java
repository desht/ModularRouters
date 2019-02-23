package me.desht.modularrouters.block.tile;

import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.util.Scheduler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class TileEntityTemplateFrame extends TileEntity implements ICamouflageable {
    private static final String NBT_CAMO_NAME = "CamouflageName";
    private static final String NBT_MIMIC = "Mimic";

    private IBlockState camouflage = null;  // block to masquerade as
    private boolean extendedMimic; // true if extra mimicking is done (light, hardness, blast resistance)

    public TileEntityTemplateFrame() {
        super(ObjectRegistry.TEMPLATE_FRAME_TILE);
    }

    public static TileEntityTemplateFrame getTileEntitySafely(IBlockReader world, BlockPos pos) {
//        TileEntity te = world instanceof ChunkCache ?
//                ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) :
//                world.getTileEntity(pos);
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityTemplateFrame ? (TileEntityTemplateFrame) te : null;
    }

    @Override
    public IBlockState getCamouflage() {
        return camouflage;
    }

    @Override
    public void setCamouflage(IBlockState camouflage) {
        this.camouflage = camouflage;
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
    public void read(NBTTagCompound compound) {
        super.read(compound);
        camouflage = getCamoStateFromNBT(compound);
        extendedMimic = compound.getBoolean(NBT_MIMIC);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound = super.write(compound);
        compound.putBoolean(NBT_MIMIC, extendedMimic);
        return getNBTFromCamoState(compound, camouflage);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        camouflage = getCamoStateFromNBT(pkt.getNbtCompound());
        extendedMimic = pkt.getNbtCompound().getBoolean("Mimic");
        if (camouflage != null && extendedMimic && camouflage.getLightValue() > 0) {
            getWorld().checkLightFor(EnumLightType.BLOCK, getPos());
        }
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        camouflage = getCamoStateFromNBT(tag);
        extendedMimic = tag.getBoolean("Mimic");
        if (camouflage != null && extendedMimic && camouflage.getLightValue() > 0) {
            // this needs to be deferred a tick because the chunk isn't fully loaded,
            // so any attempt to relight will be ignored
            Scheduler.client().schedule(() -> getWorld().checkLightFor(EnumLightType.BLOCK, getPos()), 1L);
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, -1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();

        compound.putInt("x", pos.getX());
        compound.putInt("y", pos.getY());
        compound.putInt("z", pos.getZ());
        compound.putBoolean("Mimic", extendedMimic);

        return getNBTFromCamoState(compound, camouflage);
    }

    private static IBlockState getCamoStateFromNBT(NBTTagCompound tag) {
        if (tag.contains(NBT_CAMO_NAME)) {
            return NBTUtil.readBlockState(tag.getCompound(NBT_CAMO_NAME));
        }
        return null;
    }

    private static NBTTagCompound getNBTFromCamoState(NBTTagCompound compound, IBlockState camouflage) {
        compound.put(NBT_CAMO_NAME, NBTUtil.writeBlockState(camouflage));
        return compound;
    }

    public void setCamouflage(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlock) {
            camouflage = ((ItemBlock) itemStack.getItem()).getBlock().getDefaultState();
        }
    }
}
