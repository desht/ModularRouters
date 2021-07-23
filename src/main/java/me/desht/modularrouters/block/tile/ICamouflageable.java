package me.desht.modularrouters.block.tile;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ICamouflageable {
    BlockState getCamouflage();
    void setCamouflage(BlockState camouflage);

    default boolean extendedMimic() { return false; }
    default void setExtendedMimic(boolean mimic) {}

    static boolean isCamouflaged(BlockEntity te) {
        return te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null;
    }
}
