package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MouseOverHelp {
    private static final int TEXT_MARGIN = 8;

    private final List<HelpRegion> helpRegions = new ArrayList<>();
    private final GuiContainer screen;

    private boolean active = false;

    public MouseOverHelp(GuiContainer screen) {
        this.screen = screen;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key) {
        addHelpRegion(x1, y1, x2, y2, key, HelpRegion.YES);
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key, Predicate<GuiContainer> showPredicate) {
        helpRegions.add(new HelpRegion(x1, y1, x2, y2, MiscUtil.wrapString(I18n.format(key)), showPredicate));
    }

    private void onMouseOver(int mouseX, int mouseY) {
        if (active) {
            HelpRegion region = getRegionAt(mouseX, mouseY);
            if (region != null) {
                showPopupBox(screen, Minecraft.getInstance().fontRenderer, region.extent, 0xC0000000, 0x6040FFFF, 0x0, null);
                showPopupBox(screen, Minecraft.getInstance().fontRenderer, region.extent, 0xC0000000, 0xE0202020, 0xFFE0E0E0, region.text);
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

    private static void showPopupBox(GuiContainer screen, FontRenderer fontRenderer, Rectangle rect, int borderColor, int bgColor, int textColor, List<String> helpText) {
        int boxWidth, boxHeight;

        Rectangle rect2 = new Rectangle(rect);

        if (helpText != null && !helpText.isEmpty()) {
            boxWidth = 0;
            boxHeight = helpText.size() * fontRenderer.FONT_HEIGHT;
            for (String s : helpText) {
                boxWidth = Math.max(boxWidth, fontRenderer.getStringWidth(s));
            }
            // enlarge box width & height for a text margin
            int xOff = rect.x - screen.getGuiLeft() < screen.getXSize() / 2 ? rect.width + 10 : -(boxWidth + TEXT_MARGIN + 10);
            int yOff = (rect.height - boxHeight - TEXT_MARGIN) / 2;
            rect2.setBounds(rect.x + xOff, rect.y + yOff, boxWidth + TEXT_MARGIN, boxHeight + TEXT_MARGIN);
        }

        int x1 = rect2.x - screen.getGuiLeft();
        int y1 = rect2.y - screen.getGuiTop();
        int x2 = x1 + rect2.width;
        int y2 = y1 + rect2.height;

        GlStateManager.translatef(0f, 0f, 300f);
        Gui.drawRect(x1, y1, x2, y2, bgColor);
        Gui.drawRect(x1, y1, x2, y1 + 1, borderColor);
        Gui.drawRect(x1, y2, x2, y2 + 1, borderColor);
        Gui.drawRect(x1, y1, x1 + 1, y2, borderColor);
        Gui.drawRect(x2, y1, x2 + 1, y2 + 1, borderColor);

        if (helpText != null) {
            for (String s : helpText) {
                fontRenderer.drawString(s, x1 + TEXT_MARGIN / 2, y1 + TEXT_MARGIN / 2, textColor);
                y1 += fontRenderer.FONT_HEIGHT;
            }
        }

        GlStateManager.translatef(0f, 0f, -300f);
    }

    public static class HelpRegion {
        final Rectangle extent;
        final List<String> text;
        final Predicate<GuiContainer> showPredicate;
        static final Predicate<GuiContainer> YES = guiContainer -> true;

        HelpRegion(int x1, int y1, int x2, int y2, List<String> text) {
            this(x1, y1, x2, y2, text, YES);
        }

        HelpRegion(int x1, int y1, int x2, int y2, List<String> text, Predicate<GuiContainer> showPredicate) {
            this.extent = new Rectangle(x1, y1, x2 - x1, y2 - y1);
            this.text = text;
            this.showPredicate = showPredicate;
        }
    }

    @SubscribeEvent
    public static void drawMouseOver(GuiContainerEvent.DrawForeground event) {
        // using an event ensures this is done after all subclass drawing is done
        // otherwise help region highlights can obscure text
        if (event.getGuiContainer() instanceof IMouseOverHelpProvider) {
            ((IMouseOverHelpProvider) event.getGuiContainer()).getMouseOverHelp().onMouseOver(event.getMouseX(), event.getMouseY());
        }
    }

    public static class Button extends TexturedToggleButton {
        private final MouseOverHelp mouseOverHelp;

        public Button(int buttonId, int x, int y, MouseOverHelp mouseOverHelp) {
            super(buttonId, x, y, 16, 16, false);
            this.mouseOverHelp = mouseOverHelp;
            tooltip1.addAll(MiscUtil.wrapString(I18n.format("guiText.tooltip.mouseOverHelp.false")));
            tooltip2.addAll(MiscUtil.wrapString(I18n.format("guiText.tooltip.mouseOverHelp.true")));
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
