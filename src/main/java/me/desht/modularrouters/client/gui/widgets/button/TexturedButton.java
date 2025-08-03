package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public abstract class TexturedButton extends ExtendedButton /*implements ITooltipButton*/ {
    static final ResourceLocation TEXTURE = MiscUtil.RL("textures/gui/widgets.png");

    public TexturedButton(int x, int y, int width, int height, OnPress pressable) {
        super(x, y, width, height, Component.empty(), pressable);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            int i = getYImage(isHovered);
//            RenderSystem.enableBlend();
//            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (drawStandardBackground()) {
                graphics.blit(RenderType::guiTextured, TEXTURE, this.getX(), this.getY(), i * 16, 0, this.width, this.height, 256, 256);
            }
            graphics.blit(RenderType::guiTextured, TEXTURE, this.getX(), this.getY(), getTextureX(), getTextureY(), this.width, this.height, 256, 256);
            if (isHoveredOrFocused()) {
                GuiUtil.drawFrame(graphics, this, 0xffffffff);
            }
        }
    }

    protected int getYImage(boolean pIsHovered) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (pIsHovered) {
            i = 2;
        }

        return i;
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

}
