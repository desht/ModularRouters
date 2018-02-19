package me.desht.modularrouters.block.tile;

import net.minecraft.block.state.IBlockState;

public interface ICamouflageable {
    IBlockState getCamouflage();
    void setCamouflage(IBlockState camouflage);

    default boolean extendedMimic() { return false; }
    default void setExtendedMimic(boolean mimic) {}
}
