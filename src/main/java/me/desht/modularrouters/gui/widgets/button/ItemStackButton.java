package me.desht.modularrouters.gui.widgets.button;

import me.desht.modularrouters.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

public class ItemStackButton extends TexturedButton {
    private final ItemStack renderStack;
    private boolean flat;

    public ItemStackButton(int buttonId, int x, int y, int width, int height, ItemStack renderStack, boolean flat) {
        super(buttonId, x, y, width, height);
        this.renderStack = renderStack;
        this.flat = flat;
    }

    public ItemStack getRenderStack() {
        return renderStack;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float p3) {
        mc.getTextureManager().bindTexture(resourceLocation);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int i = this.getHoverState(this.hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        if (!flat) {
            this.drawTexturedModalRect(this.x, this.y, i * 16, 0, this.width, this.height);
        }
        int x = this.x + (width - 18) / 2;
        int y = this.y + (height - 18) / 2;
        RenderHelper.renderItemStack(mc, getRenderStack(), x, y, "");
        this.mouseDragged(mc, mouseX, mouseY);
    }

    @Override
    protected int getTextureX() {
        return 0;
    }

    @Override
    protected int getTextureY() {
        return 0;
    }
}
