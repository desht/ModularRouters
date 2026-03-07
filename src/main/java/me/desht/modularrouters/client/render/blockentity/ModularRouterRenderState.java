package me.desht.modularrouters.client.render.blockentity;

import me.desht.modularrouters.util.BeamData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class ModularRouterRenderState extends BlockEntityRenderState {
    public List<RenderedBeamData> beams;
    @Nullable
    public BlockState camouflage;

    public record RenderedBeamData(BeamData beamData, float progress, ItemStackRenderState renderState) {
        public static RenderedBeamData create(BeamData beamData, Level level, float partial, ItemModelResolver resolver) {
            ItemStackRenderState state = new ItemStackRenderState();
            resolver.updateForTopItem(state, beamData.stack(), ItemDisplayContext.GROUND, level, null, 0);
            return new RenderedBeamData(beamData, beamData.getProgress(partial), state);
        }
    }
}
