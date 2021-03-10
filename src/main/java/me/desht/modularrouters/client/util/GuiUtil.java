package me.desht.modularrouters.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

public class GuiUtil {
    public static void renderItemStack(MatrixStack matrixStack, Minecraft mc, ItemStack stack, int x, int y, String txt) {
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        if (!stack.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.translate(0.0F, 0.0F, 32.0F);
            itemRender.renderAndDecorateItem(stack, x, y);
            itemRender.renderGuiItemDecorations(mc.font, stack, x, y, txt);
            matrixStack.popPose();
        }
    }
}
