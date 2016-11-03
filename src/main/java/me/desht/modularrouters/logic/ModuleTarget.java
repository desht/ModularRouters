package me.desht.modularrouters.logic;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

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
        ext.setInteger("Dimension", dimId);
        ext.setInteger("X", pos.getX());
        ext.setInteger("Y", pos.getY());
        ext.setInteger("Z", pos.getZ());
        ext.setByte("Face", (byte) face.ordinal());
        ext.setString("InvName", invName);
        return ext;
    }

    public static ModuleTarget fromNBT(NBTTagCompound nbt) {
        BlockPos pos = new BlockPos(nbt.getInteger("X"), nbt.getInteger("Y"), nbt.getInteger("Z"));
        EnumFacing face = EnumFacing.values()[nbt.getByte("Face")];
        return new ModuleTarget(nbt.getInteger("Dimension"), pos, face, nbt.getString("InvName"));
    }

    @Override
    public String toString() {
        return MiscUtil.locToString(dimId, pos) + " " + face + " [" + invName + "]";
    }
}
