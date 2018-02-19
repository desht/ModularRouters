package me.desht.modularrouters.block.tile;

import me.desht.modularrouters.util.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;

public class TileEntityTemplateFrame extends TileEntity implements ICamouflageable {
    private static final String NBT_CAMO_NAME = "CamouflageName";
    private static final String NBT_CAMO_META = "CamouflageMeta";
    private static final String NBT_MIMIC = "Mimic";

    private IBlockState camouflage = null;  // block to masquerade as
    private boolean extendedMimic; // true if extra mimicking is done (light, hardness, blast resistance)

    public TileEntityTemplateFrame() {
        super();
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("tile.templateFrame.name");
    }

    public static TileEntityTemplateFrame getTileEntitySafely(IBlockAccess world, BlockPos pos) {
        TileEntity te = world instanceof ChunkCache ?
                ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) :
                world.getTileEntity(pos);
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
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        camouflage = getCamoStateFromNBT(compound);
        extendedMimic = compound.getBoolean(NBT_MIMIC);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setBoolean(NBT_MIMIC, extendedMimic);
        return getNBTFromCamoState(compound, camouflage);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        camouflage = getCamoStateFromNBT(pkt.getNbtCompound());
        extendedMimic = pkt.getNbtCompound().getBoolean("Mimic");
        if (camouflage != null && extendedMimic && camouflage.getBlock().getLightValue(camouflage) > 0) {
            getWorld().checkLightFor(EnumSkyBlock.BLOCK, getPos());
        }
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        camouflage = getCamoStateFromNBT(tag);
        extendedMimic = tag.getBoolean("Mimic");
        if (camouflage != null && extendedMimic && camouflage.getBlock().getLightValue(camouflage) > 0) {
            // this needs to be deferred a tick because the chunk isn't fully loaded,
            // so any attempt to relight will be ignored
            Scheduler.client().schedule(() -> getWorld().checkLightFor(EnumSkyBlock.BLOCK, getPos()), 1L);
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, getBlockMetadata(), getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("x", pos.getX());
        compound.setInteger("y", pos.getY());
        compound.setInteger("z", pos.getZ());
        compound.setBoolean("Mimic", extendedMimic);

        return getNBTFromCamoState(compound, camouflage);
    }

    private static IBlockState getCamoStateFromNBT(NBTTagCompound tag) {
        if (tag.hasKey(NBT_CAMO_NAME)) {
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString(NBT_CAMO_NAME)));
            return b != null ? b.getStateFromMeta(tag.getInteger(NBT_CAMO_META)) : null;
        }
        return null;
    }

    private static NBTTagCompound getNBTFromCamoState(NBTTagCompound compound, IBlockState camouflage) {
        if (camouflage != null) {
            Block b = camouflage.getBlock();
            compound.setString(NBT_CAMO_NAME, b.getRegistryName().toString());
            compound.setInteger(NBT_CAMO_META, b.getMetaFromState(camouflage));
        }
        return compound;
    }

    public void setCamouflage(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlock) {
            camouflage = ((ItemBlock) itemStack.getItem()).getBlock().getStateFromMeta(itemStack.getMetadata());
        }
    }
}
