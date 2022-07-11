package me.desht.modularrouters.client.gui.widgets.button;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.XYPoint;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.List;

public abstract class TexturedButton extends ExtendedButton implements ITooltipButton {
    static final ResourceLocation TEXTURE = new ResourceLocation(ModularRouters.MODID, "textures/gui/widgets.png");

    protected final List<Component> tooltip1;

    public TexturedButton(int x, int y, int width, int height, OnPress pressable) {
        super(x, y, width, height, Component.empty(), pressable);
        this.tooltip1 = new ArrayList<>();
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            GuiUtil.bindTexture(TEXTURE);
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getYImage(this.isHovered);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (drawStandardBackground()) {
                blit(matrixStack, this.x, this.y, i * 16, 0, this.width, this.height);
            }
            blit(matrixStack, this.x, this.y, getTextureX(), getTextureY(), this.width, this.height);
        }
    }

    protected boolean drawStandardBackground() {
        return true;
    }

    protected abstract XYPoint getTextureXY();

    final int getTextureX() {
        return getTextureXY().x();
    }

    final int getTextureY() {
        return getTextureXY().y();
    }

    public List<Component> getTooltip() {
        return tooltip1;
    }
}
