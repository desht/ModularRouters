package me.desht.modularrouters.client.gui.widgets;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.energy.IEnergyStorage;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class EnergyWidget extends AbstractWidget {
    private static final ResourceLocation TEXTURE_LOCATION = RL("textures/gui/energy_widget.png");

    private static final int DEFAULT_SCALE = 64;

    private final IEnergyStorage storage;

    public EnergyWidget(int x, int y, IEnergyStorage storage) {
        super(x, y, 16, DEFAULT_SCALE, Component.empty());
        this.storage = storage;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        graphics.blit(TEXTURE_LOCATION, getX() + 1, getY(), 1, 0, width - 2, height, 32, 64);
        graphics.blit(TEXTURE_LOCATION, getX() + 1, getY() + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, width - 2, amount, 32, 64);

        if (isHovered()) {
            // drawing the tooltip directly instead of using setTooltip() - that causes awful flickering if
            // energy levels are changing fast
            Component text = Component.literal(MiscUtil.commify(storage.getEnergyStored()) + " / " + MiscUtil.commify(storage.getMaxEnergyStored()) + " FE");
            graphics.renderTooltip(Minecraft.getInstance().font, text, mouseX, mouseY);
        }
    }

    private int getScaled(){
        if (storage.getMaxEnergyStored() <= 0) {
            return height;
        }
        // avoid integer overflow here
        return (int)((long)storage.getEnergyStored() * height / storage.getMaxEnergyStored());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
