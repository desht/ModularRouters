package me.desht.modularrouters.client.gui.module;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.InfoButton;
import me.desht.modularrouters.container.ModuleMenu;
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
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        this.blit(matrixStack, leftPos + 128, topPos + 16, 202, 52, 54, 54);
    }
}
