package me.desht.modularrouters.logic;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Objects;

/**
 * Represents the blockpos that a module in a particular router has targeted, including the dimension
 * and face of the block where insertion/extraction will occur.
 */
public class ModuleTarget {
    public final int dimId;
    public final BlockPos pos;
    public final Direction face;
    public final String blockTranslationKey;

    public ModuleTarget(int dimId, BlockPos pos, Direction face, String blockTranslationKey) {
        this.dimId = dimId;
        this.pos = pos;
        this.face = face;
        this.blockTranslationKey = blockTranslationKey;
    }

    public ModuleTarget(int dimId, BlockPos pos, Direction face) {
        this(dimId, pos, face, "");
    }

    public ModuleTarget(int dimId, BlockPos pos) {
        this(dimId, pos, null);
    }

    public CompoundNBT toNBT() {
        CompoundNBT ext = new CompoundNBT();
        ext.putInt("Dimension", dimId);
        ext.putInt("X", pos.getX());
        ext.putInt("Y", pos.getY());
        ext.putInt("Z", pos.getZ());
        ext.putByte("Face", (byte) face.ordinal());
        ext.putString("InvName", blockTranslationKey);
        return ext;
    }

    public static ModuleTarget fromNBT(CompoundNBT nbt) {
        BlockPos pos = new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
        Direction face = Direction.values()[nbt.getByte("Face")];
        return new ModuleTarget(nbt.getInt("Dimension"), pos, face, nbt.getString("InvName"));
    }

    public IItemHandler getItemHandler() {
        DimensionType dt = DimensionType.getById(dimId);
        if (dt == null) {
            return null;
        }
        ServerWorld w = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), dt, true, true);
        // getChunkProvider
        if (w == null || !w.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4))
            return null;
        TileEntity te = w.getTileEntity(pos);
        if (te == null) {
            return null;
        }
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).orElse(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleTarget)) return false;
        ModuleTarget that = (ModuleTarget) o;
        return dimId == that.dimId &&
                Objects.equals(pos, that.pos) &&
                face == that.face;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimId, pos, face);
    }

    @Override
    public String toString() {
        String s = blockTranslationKey == null || blockTranslationKey.isEmpty() ? "" : " [" + blockTranslationKey + "]";
        return MiscUtil.locToString(dimId, pos) + " " + face + s;
    }

    public ITextComponent getTextComponent() {
        return new StringTextComponent(MiscUtil.locToString(dimId, pos) + " " + face)
                .appendText(" [")
                .appendSibling(MiscUtil.xlate(blockTranslationKey))
                .appendText("]")
                .applyTextStyle(TextFormatting.AQUA);
    }
}
