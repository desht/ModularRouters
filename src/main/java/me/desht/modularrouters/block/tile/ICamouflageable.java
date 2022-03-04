package me.desht.modularrouters.block.tile;

import net.minecraft.world.level.block.state.BlockState;

public interface ICamouflageable {
    BlockState getCamouflage();

    default boolean extendedMimic() { return false; }
}
