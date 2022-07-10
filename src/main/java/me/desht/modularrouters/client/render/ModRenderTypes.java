package me.desht.modularrouters.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable pre, Runnable post) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, pre, post);
    }

    private static final LineStateShard THICK_LINE = new LineStateShard(OptionalDouble.of(10.0));
    private static final LineStateShard THIN_LINE = new LineStateShard(OptionalDouble.of(3.0));

    public static final RenderType BEAM_LINE_THICK = create("beam_line_thick",
            DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setLineState(THICK_LINE)
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );
    public static final RenderType BEAM_LINE_THIN = create("beam_line_thin",
            DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setLineState(THIN_LINE)
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_HILIGHT_FACE = create("block_hilight",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_HILIGHT_LINE = create("block_hilight_line",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256, false, false,
            RenderType.CompositeState.builder().setLineState(THIN_LINE)
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );
}
