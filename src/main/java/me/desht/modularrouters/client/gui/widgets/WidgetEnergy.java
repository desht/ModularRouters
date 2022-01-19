package me.desht.modularrouters.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Collections;
import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class WidgetEnergy extends AbstractWidget implements ITooltipButton {
    private static final ResourceLocation TEXTURE_LOCATION = RL("textures/gui/energy_widget.png");

    private static final int DEFAULT_SCALE = 64;

    private final IEnergyStorage storage;

    public WidgetEnergy(int x, int y, IEnergyStorage storage) {
        super(x, y, 16, DEFAULT_SCALE, TextComponent.EMPTY);
        this.storage = storage;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        GuiUtil.bindTexture(TEXTURE_LOCATION);
        GuiComponent.blit(matrixStack, x + 1, y, 1, 0, width - 2, height, 32, 64);
        GuiComponent.blit(matrixStack, x + 1, y + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, width - 2, amount, 32, 64);
    }

    private int getScaled(){
        if (storage.getMaxEnergyStored() <= 0) {
            return height;
        }
        // avoid integer overflow here
        return (int)((long)storage.getEnergyStored() * height / storage.getMaxEnergyStored());
    }

    @Override
    public List<Component> getTooltip() {
        return Collections.singletonList(new TextComponent(MiscUtil.commify(storage.getEnergyStored()) + " / " + MiscUtil.commify(storage.getMaxEnergyStored()) + " FE"));
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
