package me.desht.modularrouters.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

public class GuiUtil {
    public static void renderItemStack(MatrixStack matrixStack, Minecraft mc, ItemStack stack, int x, int y, String txt) {
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        if (!stack.isEmpty()) {
            matrixStack.push();
            matrixStack.translate(0.0F, 0.0F, 32.0F);
            itemRender.renderItemAndEffectIntoGUI(stack, x, y);
            itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, txt);
            matrixStack.pop();
        }
    }
}
