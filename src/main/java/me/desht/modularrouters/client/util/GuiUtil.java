package me.desht.modularrouters.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuiUtil {
    public static final String TRANSLATION_LINE_BREAK = "${br}";

    public static void bindTexture(ResourceLocation texture, float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void bindTexture(ResourceLocation texture) {
        bindTexture(texture, 1f, 1f, 1f, 1f);
    }

    public static void renderItemStack(PoseStack poseStack, Minecraft mc, ItemStack stack, int x, int y, String txt) {
        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 32.0F);
            itemRender.renderAndDecorateItem(poseStack, stack, x, y);
            itemRender.renderGuiItemDecorations(poseStack, mc.font, stack, x, y, txt);
            poseStack.popPose();
        }
    }

    public static List<Component> xlateAndSplit(String key, Object... params) {
        return Arrays.stream(StringUtils.splitByWholeSeparator(I18n.get(key, params), TRANSLATION_LINE_BREAK))
                .map(Component::literal)
                .collect(Collectors.toList());
    }
}
