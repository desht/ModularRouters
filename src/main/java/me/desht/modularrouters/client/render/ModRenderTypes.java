package me.desht.modularrouters.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderState.LineState;
import net.minecraft.client.renderer.RenderState.TransparencyState;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String name, VertexFormat format, int drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable pre, Runnable post) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, pre, post);
    }

    private static final LineState THICK_LINE = new LineState(OptionalDouble.of(10.0));
    private static final LineState THIN_LINE = new LineState(OptionalDouble.of(3.0));

    public static final RenderType BEAM_LINE_THICK = create("beam_line_thick",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.builder().setLineState(THICK_LINE)
                    .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderState.NO_LIGHTMAP)
                    .setTextureState(RenderState.NO_TEXTURE)
                    .setWriteMaskState(RenderState.COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );
    public static final RenderType BEAM_LINE_THIN = create("beam_line_thin",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.builder().setLineState(THIN_LINE)
                    .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderState.NO_LIGHTMAP)
                    .setTextureState(RenderState.NO_TEXTURE)
                    .setWriteMaskState(RenderState.COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_HILIGHT_FACE = create("block_hilight",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.builder()
                    .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final RenderType BLOCK_HILIGHT_LINE = create("block_hilight_line",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.builder().setLineState(THIN_LINE)
                    .setLayeringState(RenderState.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );
}
