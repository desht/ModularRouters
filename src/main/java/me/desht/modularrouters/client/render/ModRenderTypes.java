package me.desht.modularrouters.client.render;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class ModRenderTypes {
    public static final float THICK_LINE = 10.0f;
    public static final float THIN_LINE = 3.0f;

    public static final RenderType BEAM_LINE =  RenderType.create(
            "beam_line_thin",
            RenderSetup.builder(RenderPipelines.LINES)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .bufferSize(1536)
                    .createRenderSetup()
    );

    public static final RenderType BLOCK_HILIGHT_FACE = RenderType.create(
            "block_hilight_face",
            RenderSetup.builder(ModRenderPipelines.DEBUG_QUADS_NO_DEPTH)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .bufferSize(256)
                    .createRenderSetup()
            );

    public static final RenderType BLOCK_HILIGHT_LINE = RenderType.create(
            "block_hilight_line",
            RenderSetup.builder(ModRenderPipelines.LINES_NO_DEPTH)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .bufferSize(256)
                    .createRenderSetup()
    );
}
