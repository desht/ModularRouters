package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public abstract class TexturedButton extends ExtendedButton {
    static final Identifier TEXTURE = MiscUtil.RL("textures/gui/widgets.png");

    public TexturedButton(int x, int y, int width, int height, OnPress pressable) {
        super(x, y, width, height, Component.empty(), pressable);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            int i = getYImage(isHovered);
            if (drawStandardBackground()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), i * 16, 0, this.width, this.height, 256, 256);
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), getTextureX(), getTextureY(), this.width, this.height, 256, 256);
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
