package me.desht.modularrouters.gui.widgets;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class TexturedButton extends GuiButton implements ITooltipButton {
    protected final ResourceLocation resourceLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/widgets.png");
    protected final List<String> tooltip1;

    public TexturedButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
        this.tooltip1 = new ArrayList<>();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(resourceLocation);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        int i = this.getHoverState(this.hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.drawTexturedModalRect(this.xPosition, this.yPosition, i * 16, 0, this.width, this.height);
        this.drawTexturedModalRect(this.xPosition, this.yPosition, getTextureX(), getTextureY(), this.width, this.height);
        this.mouseDragged(mc, mouseX, mouseY);
    }

    protected abstract int getTextureX();

    protected abstract int getTextureY();

    public List<String> getTooltip() {
        return tooltip1;
    }
}
