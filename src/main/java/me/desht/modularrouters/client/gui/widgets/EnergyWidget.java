package me.desht.modularrouters.client.gui.widgets;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class EnergyWidget extends AbstractWidget {
    private static final Identifier TEXTURE_LOCATION = RL("textures/gui/energy_widget.png");

    private static final int DEFAULT_SCALE = 64;

    private final EnergyHandler storage;

    public EnergyWidget(int x, int y, EnergyHandler storage) {
        super(x, y, 16, DEFAULT_SCALE, Component.empty());
        this.storage = storage;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, getX() + 1, getY(), 1, 0, width - 2, height, 32, 64);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, getX() + 1, getY() + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, width - 2, amount, 32, 64);

        if (isHovered()) {
            // drawing the tooltip directly instead of using setTooltip() - that causes awful flickering if
            // energy levels are changing fast
            Component text = Component.literal(MiscUtil.commify(storage.getAmountAsInt()) + " / " + MiscUtil.commify(storage.getCapacityAsInt()) + " FE");
            graphics.setTooltipForNextFrame(text, mouseX, mouseY);
        }
    }

    private int getScaled(){
        if (storage.getCapacityAsInt() <= 0) {
            return height;
        }
        // avoid integer overflow here
        return (int)((long)storage.getAmountAsInt() * height / storage.getCapacityAsInt());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
