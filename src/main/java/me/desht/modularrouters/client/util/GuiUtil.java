package me.desht.modularrouters.client.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiUtil {
    public static final String TRANSLATION_LINE_BREAK = "${br}";

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

    public static List<ITextComponent> xlateAndSplit(String key, Object... params) {
        return Arrays.stream(StringUtils.splitByWholeSeparator(I18n.get(key, params), TRANSLATION_LINE_BREAK))
                .map(StringTextComponent::new)
                .collect(Collectors.toList());
    }
}
