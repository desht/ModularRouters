package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.XYPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class MouseOverHelp {
    private static final int TEXT_MARGIN = 8;

    private final List<HelpRegion> helpRegions = new ArrayList<>();
    private final AbstractContainerScreen<?> screen;

    private boolean active = false;

    public MouseOverHelp(AbstractContainerScreen<?> screen) {
        this.screen = screen;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key) {
        addHelpRegion(x1, y1, x2, y2, key, HelpRegion.YES);
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, Component key) {
        addHelpRegion(x1, y1, x2, y2, key, HelpRegion.YES);
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key, Predicate<AbstractContainerScreen<?>> showPredicate) {
        addHelpRegion(x1, y1, x2, y2, xlate(key), showPredicate);
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, Component key, Predicate<AbstractContainerScreen<?>> showPredicate) {
        List<FormattedCharSequence> l = ComponentRenderUtils.wrapComponents(key, screen.getXSize(), screen.getMinecraft().font);
        helpRegions.add(HelpRegion.create(x1, y1, x2, y2, l, showPredicate));
    }

    private void onMouseOver(GuiGraphics graphics, int mouseX, int mouseY) {
        if (active) {
            HelpRegion region = getRegionAt(mouseX, mouseY);
            if (region != null) {
                showPopupBox(graphics, screen, Minecraft.getInstance().font, region.extent, 0xC0000000, 0x6040FFFF, 0x0, null);
                showPopupBox(graphics, screen, Minecraft.getInstance().font, region.extent, 0xC0000000, 0xE0202020, 0xFFE0E0E0, region.text);
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

    private static Rect2i calcBounds(AbstractContainerScreen<?> screen, Font fontRenderer, Rect2i rect, List<FormattedCharSequence> helpText) {
        int boxWidth, boxHeight;

        if (helpText != null && !helpText.isEmpty()) {
            boxWidth = 0;
            boxHeight = helpText.size() * fontRenderer.lineHeight;
            for (FormattedCharSequence s : helpText) {
                boxWidth = Math.max(boxWidth, fontRenderer.width(s));
            }
            // enlarge box width & height for a text margin
            int xOff = rect.getX() - screen.getGuiLeft() < screen.getXSize() / 2 ? rect.getWidth() + 10 : -(boxWidth + TEXT_MARGIN + 10);
            int yOff = (rect.getHeight() - boxHeight - TEXT_MARGIN) / 2;
            return new Rect2i(rect.getX() + xOff, rect.getY() + yOff, boxWidth + TEXT_MARGIN, boxHeight + TEXT_MARGIN);
        } else {
            return rect;
        }
    }

    private static void showPopupBox(GuiGraphics graphics, AbstractContainerScreen<?> screen, Font fontRenderer, Rect2i rect, int borderColor, int bgColor, int textColor, List<FormattedCharSequence> helpText) {
        Rect2i actualRect = calcBounds(screen, fontRenderer, rect, helpText);

        int x1 = actualRect.getX() - screen.getGuiLeft();
        int y1 = actualRect.getY() - screen.getGuiTop();
        int x2 = x1 + actualRect.getWidth();
        int y2 = y1 + actualRect.getHeight();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        graphics.fill(x1, y1, x2, y2, bgColor);
        graphics.fill(x1, y1, x2, y1 + 1, borderColor);
        graphics.fill(x1, y2, x2, y2 + 1, borderColor);
        graphics.fill(x1, y1, x1 + 1, y2, borderColor);
        graphics.fill(x2, y1, x2 + 1, y2 + 1, borderColor);

        if (helpText != null) {
            for (FormattedCharSequence s : helpText) {
                graphics.drawString(fontRenderer, s, x1 + TEXT_MARGIN / 2, y1 + TEXT_MARGIN / 2, textColor);
                y1 += fontRenderer.lineHeight;
            }
        }

        graphics.pose().popPose();
    }

    public record HelpRegion(Rect2i extent, List<FormattedCharSequence> text, Predicate<AbstractContainerScreen<?>> showPredicate) {
        static final Predicate<AbstractContainerScreen<?>> YES = guiContainer -> true;

        static HelpRegion create(int x1, int y1, int x2, int y2, List<FormattedCharSequence> text, Predicate<AbstractContainerScreen<?>> showPredicate) {
            return new HelpRegion(new Rect2i(x1, y1, x2 -x1, y2 - y1), text, showPredicate);
        }
    }

    @SubscribeEvent
    public static void drawMouseOver(ContainerScreenEvent.Render.Foreground event) {
        // using an event ensures this is done after all subclass drawing is done
        // otherwise help region highlights can obscure text
        if (event.getContainerScreen() instanceof IMouseOverHelpProvider provider) {
            provider.getMouseOverHelp().onMouseOver(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
        }
    }

    public static class Button extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(192, 0);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(208, 0);

        public Button(int x, int y) {
            super(x, y, 16, 16, false, null);
            setTooltips(xlate("modularrouters.guiText.tooltip.mouseOverHelp.false"), xlate("modularrouters.guiText.tooltip.mouseOverHelp.true"));
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
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }
}
