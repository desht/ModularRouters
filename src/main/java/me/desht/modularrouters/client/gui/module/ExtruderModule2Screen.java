package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.InfoButton;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ModuleMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExtruderModule2Screen extends AbstractModuleScreen {
    public ExtruderModule2Screen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new InfoButton(leftPos + 173, topPos + 70, "extruder2.template"));

        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 16, leftPos + 181, topPos + 69, "modularrouters.guiText.popup.extruder2.template");
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
        TintColor c = getGuiBackgroundTint();
        graphics.setColor(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0F);
        graphics.blit(GUI_TEXTURE, leftPos + 128, topPos + 16, 202, 52, 54, 54);
        graphics.setColor(1f, 1f, 1f, 1f);

    }
}
