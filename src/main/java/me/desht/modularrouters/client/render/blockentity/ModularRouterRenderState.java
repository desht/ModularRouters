package me.desht.modularrouters.client.render.blockentity;

import me.desht.modularrouters.util.BeamData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class ModularRouterRenderState extends BlockEntityRenderState {
    public List<BeamData.WithProgress> beams;
    @Nullable
    public BlockState camouflage;
}
