package me.desht.modularrouters.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Represents the blockpos that a module in a particular router has targeted, including the dimension
 * and face of the block where insertion/extraction will occur.
 */
public class RouterTarget {
    public final int dimId;
    public final BlockPos pos;
    public final EnumFacing face;

    public RouterTarget(int dimId, int x, int y, int z, EnumFacing face) {
        this.dimId = dimId;
        this.pos = new BlockPos(x, y, z);
        this.face = face;
    }

    public RouterTarget(int dimension, BlockPos position, EnumFacing face) {
        this(dimension, position.getX(), position.getY(), position.getZ(), face);
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound ext = new NBTTagCompound();
        ext.setInteger("Dim", dimId);
        ext.setInteger("X", pos.getX());
        ext.setInteger("Y", pos.getY());
        ext.setInteger("Z", pos.getZ());
        ext.setByte("Face", (byte) face.ordinal());
        return ext;
    }

    public static RouterTarget fromNBT(NBTTagCompound nbt) {
        return new RouterTarget(nbt.getInteger("Dim"),
                nbt.getInteger("X"), nbt.getInteger("Y"), nbt.getInteger("Z"),
                EnumFacing.values()[nbt.getByte("Face")]);
    }
}
