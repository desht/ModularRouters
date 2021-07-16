package me.desht.modularrouters.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

public interface ICamouflageable {
    BlockState getCamouflage();
    void setCamouflage(BlockState camouflage);

    default boolean extendedMimic() { return false; }
    default void setExtendedMimic(boolean mimic) {}

    static boolean isCamouflaged(TileEntity te) {
        return te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null;
    }
}
