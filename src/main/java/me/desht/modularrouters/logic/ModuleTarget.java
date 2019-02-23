package me.desht.modularrouters.logic;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
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
    public final EnumFacing face;
    public final String invName;

    public ModuleTarget(int dimId, BlockPos pos, EnumFacing face, String invName) {
        this.dimId = dimId;
        this.pos = pos;
        this.face = face;
        this.invName = invName;
    }

    public ModuleTarget(int dimId, BlockPos pos, EnumFacing face) {
        this(dimId, pos, face, "");
    }

    public ModuleTarget(int dimId, BlockPos pos) {
        this(dimId, pos, null);
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound ext = new NBTTagCompound();
        ext.putInt("Dimension", dimId);
        ext.putInt("X", pos.getX());
        ext.putInt("Y", pos.getY());
        ext.putInt("Z", pos.getZ());
        ext.putByte("Face", (byte) face.ordinal());
        ext.putString("InvName", invName);
        return ext;
    }

    public static ModuleTarget fromNBT(NBTTagCompound nbt) {
        BlockPos pos = new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
        EnumFacing face = EnumFacing.values()[nbt.getByte("Face")];
        return new ModuleTarget(nbt.getInt("Dimension"), pos, face, nbt.getString("InvName"));
    }

    public IItemHandler getItemHandler() {
        DimensionType dt = DimensionType.getById(dimId);
        if (dt == null) {
            return null;
        }
        WorldServer w = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), dt, true, true);
        if (w == null || !w.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4))
            return null;
        TileEntity te = w.getTileEntity(pos);
        if (te == null) {
            return null;
        }
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).orElse(null);
//        if (te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face)) {
//            return null;
//        }
//        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
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
        String s = invName == null || invName.isEmpty() ? "" : " [" + invName + "]";
        return MiscUtil.locToString(dimId, pos) + " " + face + s;
    }
}
