package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class TexturedButton extends GuiButton implements ITooltipButton {
    protected static final ResourceLocation resourceLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/widgets.png");
    protected final List<String> tooltip1;

    public TexturedButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "");
        this.tooltip1 = new ArrayList<>();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float p3) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(resourceLocation);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (drawStandardBackground()) {
                this.drawTexturedModalRect(this.x, this.y, i * 16, 0, this.width, this.height);
            }
            this.drawTexturedModalRect(this.x, this.y, getTextureX(), getTextureY(), this.width, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    protected boolean drawStandardBackground() {
        return true;
    }

    protected abstract int getTextureX();

    protected abstract int getTextureY();

    public List<String> getTooltip() {
        return tooltip1;
    }
}
