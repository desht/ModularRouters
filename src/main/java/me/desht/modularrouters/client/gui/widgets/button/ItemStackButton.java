package me.desht.modularrouters.client.gui.widgets.button;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.XYPoint;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class ItemStackButton extends TexturedButton {
    private static final XYPoint TEXTURE_XY = new XYPoint(0, 0);

    private final ItemStack renderStack;
    private final boolean flat;

    public ItemStackButton(int x, int y, int width, int height, ItemStack renderStack, boolean flat, OnPress pressable) {
        super(x, y, width, height, pressable);
        this.renderStack = renderStack;
        this.flat = flat;
    }

    public ItemStack getRenderStack() {
        return renderStack;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (!flat) {
                graphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
            int x = this.getX() + (width - 16) / 2;
            int y = this.getY() + (height - 16) / 2;
            graphics.renderItem(getRenderStack(), x, y);
            if (isHoveredOrFocused()) {
                GuiUtil.drawFrame(graphics, this, 0xffffffff);
            }
        }
    }

    @Override
    protected XYPoint getTextureXY() {
        return TEXTURE_XY;
    }
}
