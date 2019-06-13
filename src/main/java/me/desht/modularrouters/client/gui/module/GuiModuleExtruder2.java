package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.InfoButton;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerModule;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiModuleExtruder2 extends GuiModule {
    public GuiModuleExtruder2(ContainerModule container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        addButton(new InfoButton(guiLeft + 173, guiTop + 70, "extruder2.template"));

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 16, guiLeft + 181, guiTop + 69, "guiText.popup.extruder2.template");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.blit(guiLeft + 128, guiTop + 16, 202, 52, 54, 54);
    }
}
