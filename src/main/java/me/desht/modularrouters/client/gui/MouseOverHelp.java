package me.desht.modularrouters.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MouseOverHelp {
    private static final int TEXT_MARGIN = 8;

    private final List<HelpRegion> helpRegions = new ArrayList<>();
    private final ContainerScreen<?> screen;

    private boolean active = false;

    public MouseOverHelp(ContainerScreen<?> screen) {
        this.screen = screen;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key) {
        addHelpRegion(x1, y1, x2, y2, key, HelpRegion.YES);
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key, Predicate<ContainerScreen<?>> showPredicate) {
        helpRegions.add(new HelpRegion(x1, y1, x2, y2, MiscUtil.wrapString(I18n.get(key), 35), showPredicate));
    }

    private void onMouseOver(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (active) {
            HelpRegion region = getRegionAt(mouseX, mouseY);
            if (region != null) {
                showPopupBox(matrixStack, screen, Minecraft.getInstance().font, region.extent, 0xC0000000, 0x6040FFFF, 0x0, null);
                showPopupBox(matrixStack, screen, Minecraft.getInstance().font, region.extent, 0xC0000000, 0xE0202020, 0xFFE0E0E0, region.text);
            }
        }
    }

    private HelpRegion getRegionAt(int mouseX, int mouseY) {
        for (HelpRegion region : helpRegions) {
            if (region.extent.contains(mouseX, mouseY) && region.showPredicate.test(screen)) {
                return region;
            }
        }
        return null;
    }

    private static Rectangle2d calcBounds(ContainerScreen<?> screen, FontRenderer fontRenderer, Rectangle2d rect, List<String> helpText) {
        int boxWidth, boxHeight;

        if (helpText != null && !helpText.isEmpty()) {
            boxWidth = 0;
            boxHeight = helpText.size() * fontRenderer.lineHeight;
            for (String s : helpText) {
                boxWidth = Math.max(boxWidth, fontRenderer.width(s));
            }
            // enlarge box width & height for a text margin
            int xOff = rect.getX() - screen.getGuiLeft() < screen.getXSize() / 2 ? rect.getWidth() + 10 : -(boxWidth + TEXT_MARGIN + 10);
            int yOff = (rect.getHeight() - boxHeight - TEXT_MARGIN) / 2;
            return new Rectangle2d(rect.getX() + xOff, rect.getY() + yOff, boxWidth + TEXT_MARGIN, boxHeight + TEXT_MARGIN);
        } else {
            return rect;
        }
    }

    private static void showPopupBox(MatrixStack matrixStack, ContainerScreen<?> screen, FontRenderer fontRenderer, Rectangle2d rect, int borderColor, int bgColor, int textColor, List<String> helpText) {
        Rectangle2d actualRect = calcBounds(screen, fontRenderer, rect, helpText);

        int x1 = actualRect.getX() - screen.getGuiLeft();
        int y1 = actualRect.getY() - screen.getGuiTop();
        int x2 = x1 + actualRect.getWidth();
        int y2 = y1 + actualRect.getHeight();

        matrixStack.pushPose();
        matrixStack.translate(0, 0, 300);
        AbstractGui.fill(matrixStack, x1, y1, x2, y2, bgColor);
        AbstractGui.fill(matrixStack, x1, y1, x2, y1 + 1, borderColor);
        AbstractGui.fill(matrixStack, x1, y2, x2, y2 + 1, borderColor);
        AbstractGui.fill(matrixStack, x1, y1, x1 + 1, y2, borderColor);
        AbstractGui.fill(matrixStack, x2, y1, x2 + 1, y2 + 1, borderColor);

        if (helpText != null) {
            for (String s : helpText) {
                fontRenderer.draw(matrixStack, s, x1 + TEXT_MARGIN / 2f, y1 + TEXT_MARGIN / 2f, textColor);
                y1 += fontRenderer.lineHeight;
            }
        }

        matrixStack.popPose();
    }

    public static class HelpRegion {
        final Rectangle2d extent;
        final List<String> text;
        final Predicate<ContainerScreen<?>> showPredicate;
        static final Predicate<ContainerScreen<?>> YES = guiContainer -> true;

        HelpRegion(int x1, int y1, int x2, int y2, List<String> text) {
            this(x1, y1, x2, y2, text, YES);
        }

        HelpRegion(int x1, int y1, int x2, int y2, List<String> text, Predicate<ContainerScreen<?>> showPredicate) {
            this.extent = new Rectangle2d(x1, y1, x2 - x1, y2 - y1);
            this.text = text;
            this.showPredicate = showPredicate;
        }
    }

    @SubscribeEvent
    public static void drawMouseOver(GuiContainerEvent.DrawForeground event) {
        // using an event ensures this is done after all subclass drawing is done
        // otherwise help region highlights can obscure text
        if (event.getGuiContainer() instanceof IMouseOverHelpProvider) {
            ((IMouseOverHelpProvider) event.getGuiContainer()).getMouseOverHelp().onMouseOver(event.getMatrixStack(), event.getMouseX(), event.getMouseY());
        }
    }

    public static class Button extends TexturedToggleButton {
        public Button(int x, int y) {
            super(x, y, 16, 16, false, null);
            tooltip1.addAll(MiscUtil.wrapStringAsTextComponent(I18n.get("modularrouters.guiText.tooltip.mouseOverHelp.false")));
            tooltip2.addAll(MiscUtil.wrapStringAsTextComponent(I18n.get("modularrouters.guiText.tooltip.mouseOverHelp.true")));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            toggle();
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        public boolean sendToServer() {
            return false;
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 208 : 192;
        }

        @Override
        protected int getTextureY() {
            return 0;
        }
    }
}
