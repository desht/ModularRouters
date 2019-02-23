package me.desht.modularrouters.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.ItemStack;

/**
 * With thanks to McJty's TheOneProbe render code!
 */
public class RenderHelper {
    public static void renderItemStack(Minecraft mc, ItemStack stack, int x, int y, String txt) {
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        GlStateManager.color3f(1.0F, 1.0F, 1.0F);

        if (stack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 0.0F, 32.0F);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableLighting();
            short short1 = 240;
            short short2 = 240;
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            OpenGlHelper.glMultiTexCoord2f(OpenGlHelper.GL_TEXTURE1, short1 / 1.0F, short2 / 1.0F);
            itemRender.renderItemAndEffectIntoGUI(stack, x, y);
            itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, txt);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
        }
    }

    public static void glColorHex(int color) {
        float alpha = (color >> 24 & 255) / 255F;
        float red = (color >> 16 & 255) / 255F;
        float green = (color >> 8 & 255) / 255F;
        float blue = (color & 255) / 255F;
        GlStateManager.color4f(red, green, blue, alpha);
    }

    public static void glColorHex(int color, int alpha) {
        glColorHex(color | alpha << 24);
    }
}
