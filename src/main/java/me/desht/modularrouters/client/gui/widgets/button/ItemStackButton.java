package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.XYPoint;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
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
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            if (!flat) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
            int x = this.getX() + (width - 16) / 2;
            int y = this.getY() + (height - 16) / 2;
            graphics.item(getRenderStack(), x, y);
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
