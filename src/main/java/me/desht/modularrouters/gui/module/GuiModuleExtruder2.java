package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GuiModuleExtruder2 extends GuiModule {
    private static final int INFO_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;

    public GuiModuleExtruder2(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleExtruder2(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new InfoButton(INFO_BUTTON_ID, guiLeft + 173, guiTop + 70));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.drawTexturedModalRect(guiLeft + 128, guiTop + 16, 202, 52, 54, 54);
    }

    private class InfoButton extends TexturedButton {
        InfoButton(int buttonId, int x, int y) {
            super(buttonId, x, y, 16, 16);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.extruder2.template");
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        protected int getTextureX() {
            return 128;
        }

        @Override
        protected int getTextureY() {
            return 0;
        }

    }
}
