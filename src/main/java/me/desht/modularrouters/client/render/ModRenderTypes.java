package me.desht.modularrouters.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

public class ModRenderTypes extends RenderType {
    public ModRenderTypes(String name, VertexFormat format, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable pre, Runnable post) {
        super(name, format, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, pre, post);
    }

    private static final LineState THICK_LINE = new LineState(OptionalDouble.of(10.0));
    private static final LineState THIN_LINE = new LineState(OptionalDouble.of(3.0));

    public static final RenderType BEAM_LINE_THICK = makeType("beam_line",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THICK_LINE)
                    .layer(RenderState.PROJECTION_LAYERING)
//                    .alpha(AlphaState.DEFAULT_ALPHA)
                    .transparency(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(RenderState.LIGHTMAP_DISABLED)
                    .texture(RenderState.NO_TEXTURE)
            .build(false)
    );
    public static final RenderType BEAM_LINE_THIN = makeType("beam_line",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256,
            RenderType.State.getBuilder().line(THIN_LINE)
                    .layer(RenderState.PROJECTION_LAYERING)
//                    .alpha(AlphaState.DEFAULT_ALPHA)
                    .transparency(TransparencyState.TRANSLUCENT_TRANSPARENCY)
                    .lightmap(RenderState.LIGHTMAP_DISABLED)
                    .texture(RenderState.NO_TEXTURE)
                    .build(false)
    );

    public static final RenderType BLOCK_HILIGHT_FACE = makeType("block_hilight",
            DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256,
            RenderType.State.getBuilder()
                    .layer(RenderState.PROJECTION_LAYERING)
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
                    .layer(RenderState.PROJECTION_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .texture(NO_TEXTURE)
                    .depthTest(DEPTH_ALWAYS)
                    .cull(CULL_DISABLED)
                    .lightmap(LIGHTMAP_DISABLED)
                    .writeMask(COLOR_WRITE)
                    .build(false)
    );
}
