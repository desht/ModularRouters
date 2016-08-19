package me.desht.modularrouters.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

/**
 * With thanks to McJty's TheOneProbe render code!
 */
public class RenderHelper {
    public static int renderItemStack(Minecraft mc, ItemStack itm, int x, int y, String txt) {
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

//        GlStateManager.color(1.0F, 1.0F, 1.0F);

        int rc = 0;
        if (itm != null && itm.getItem() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 32.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableLighting();
            short short1 = 240;
            short short2 = 240;
            net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, short1 / 1.0F, short2 / 1.0F);
            itemRender.renderItemAndEffectIntoGUI(itm, x, y);
//            renderItemOverlayIntoGUI(mc.fontRendererObj, itm, x, y, txt, txt.length() - 2);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            rc = 20;
        }

        return rc;
    }
}
