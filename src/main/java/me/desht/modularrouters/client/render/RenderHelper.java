package me.desht.modularrouters.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL13;

/**
 * With thanks to McJty's TheOneProbe render code!
 */
public class RenderHelper {
    public static void renderItemStack(MatrixStack matrixStack, Minecraft mc, ItemStack stack, int x, int y, String txt) {
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);

        if (!stack.isEmpty()) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 32.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableLighting();
            short short1 = 240;
            short short2 = 240;
            net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
            RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, short1 / 1.0F, short2 / 1.0F);
            itemRender.renderItemAndEffectIntoGUI(stack, x, y);
            itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, txt);
            RenderSystem.popMatrix();
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableLighting();
        }
    }

    private static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public static void glColorHex(int color, int alpha) {
        glColorHex(color | alpha << 24);
    }
}
