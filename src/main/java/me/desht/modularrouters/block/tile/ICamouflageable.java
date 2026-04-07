package me.desht.modularrouters.block.tile;

import me.desht.modularrouters.block.CamouflageableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

public interface ICamouflageable {
    @Nullable
    BlockState getCamouflage();

    default boolean extendedMimic() { return false; }

    static ModelData makeModelData(@Nullable BlockState camo) {
        ModelData.Builder builder = ModelData.builder();
        if (camo != null) {
            builder.with(CamouflageableBlock.CAMOUFLAGE_STATE, camo);
        }
        return builder.build();
    }
}
