package me.desht.modularrouters.client.render;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class ModRenderTypes {
    private static final RenderStateShard.LineStateShard THICK_LINE = new RenderStateShard.LineStateShard(OptionalDouble.of(10.0));
    private static final RenderStateShard.LineStateShard THIN_LINE = new RenderStateShard.LineStateShard(OptionalDouble.of(3.0));

    public static final RenderType BEAM_LINE_THICK = RenderType.create(
            "beam_line_thick",
            1536,
            RenderPipelines.LINES,
            RenderType.CompositeState.builder()
                    .setLineState(THICK_LINE)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .createCompositeState(false)
    );
    public static final RenderType BEAM_LINE_THIN =  RenderType.create(
            "beam_line_thin",
            1536,
            RenderPipelines.LINES,
            RenderType.CompositeState.builder()
                    .setLineState(THIN_LINE)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_HILIGHT_FACE = RenderType.create(
            "block_hilight_face",
            256,
            ModRenderPipelines.DEBUG_QUADS_NO_DEPTH,
            RenderType.CompositeState.builder()
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_HILIGHT_LINE = RenderType.create(
            "block_hilight_line",
            256,
            ModRenderPipelines.LINES_NO_DEPTH,
            RenderType.CompositeState.builder()
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .createCompositeState(false)
    );
}
