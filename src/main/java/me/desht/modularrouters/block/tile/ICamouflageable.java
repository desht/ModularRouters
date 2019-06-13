package me.desht.modularrouters.block.tile;

import net.minecraft.block.BlockState;

public interface ICamouflageable {
    BlockState getCamouflage();
    void setCamouflage(BlockState camouflage);

    default boolean extendedMimic() { return false; }
    default void setExtendedMimic(boolean mimic) {}
}
