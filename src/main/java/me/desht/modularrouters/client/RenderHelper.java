package me.desht.modularrouters.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

/**
 * With thanks to McJty's TheOneProbe render code!
 */
public class RenderHelper {
    public static void renderItemStack(Minecraft mc, ItemStack stack, int x, int y, String txt) {
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
//        GlStateManager.color(1.0F, 1.0F, 1.0F);

        if (stack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 32.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableLighting();
            short short1 = 240;
            short short2 = 240;
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
            itemRender.renderItemAndEffectIntoGUI(stack, x, y);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
        }

    }
}
