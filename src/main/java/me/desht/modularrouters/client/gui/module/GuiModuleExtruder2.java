package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.InfoButton;
import me.desht.modularrouters.container.ContainerModule;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

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

        buttonList.add(new InfoButton(INFO_BUTTON_ID, guiLeft + 173, guiTop + 70, "extruder2.template"));

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 16, guiLeft + 181, guiTop + 69, "guiText.popup.extruder2.template");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.drawTexturedModalRect(guiLeft + 128, guiTop + 16, 202, 52, 54, 54);
    }
}
