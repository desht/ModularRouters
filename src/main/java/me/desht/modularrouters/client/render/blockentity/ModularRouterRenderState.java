package me.desht.modularrouters.client.render.blockentity;

import me.desht.modularrouters.util.BeamData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import java.util.List;

public class ModularRouterRenderState extends BlockEntityRenderState {
    public List<BeamData.WithProgress> beams;
}
