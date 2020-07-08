package me.desht.modularrouters.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String name, VertexFormat format, int drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable pre, Runnable post) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, pre, post);
    }

    private static final LineState THICK_LINE = new LineState(OptionalDouble.of(10.0));
    private static final LineState THIN_LINE = new LineState(OptionalDouble.of(3.0));

    public static final RenderType BEAM_LINE_THICK = makeType("beam_line_thick",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THICK_LINE)
                    .layer(RenderState.field_239235_M_)
                    .transparency(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(RenderState.LIGHTMAP_DISABLED)
                    .texture(RenderState.NO_TEXTURE)
                    .writeMask(RenderState.COLOR_DEPTH_WRITE)
                    .build(false)
    );
    public static final RenderType BEAM_LINE_THIN = makeType("beam_line_thin",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THIN_LINE)
                    .layer(RenderState.field_239235_M_)
                    .transparency(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(RenderState.LIGHTMAP_DISABLED)
                    .texture(RenderState.NO_TEXTURE)
                    .writeMask(RenderState.COLOR_DEPTH_WRITE)
                    .build(false)
    );

    public static final RenderType BLOCK_HILIGHT_FACE = makeType("block_hilight",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .layer(RenderState.field_239235_M_)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_ALWAYS)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false)
    );

    public static final RenderType BLOCK_HILIGHT_LINE = makeType("block_hilight_line",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THIN_LINE)
                    .layer(RenderState.field_239235_M_)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_ALWAYS)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false)
    );
}
