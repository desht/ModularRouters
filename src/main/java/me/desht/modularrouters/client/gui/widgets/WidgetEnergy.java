package me.desht.modularrouters.client.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Collections;
import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class WidgetEnergy extends Widget implements ITooltipButton {
    private static final ResourceLocation TEXTURE_LOCATION = RL("textures/gui/energy_widget.png");

    private static final int DEFAULT_SCALE = 64;

    private final IEnergyStorage storage;

    public WidgetEnergy(int x, int y, IEnergyStorage storage) {
        super(x, y, 16, DEFAULT_SCALE, StringTextComponent.EMPTY);
        this.storage = storage;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        Minecraft.getInstance().getTextureManager().bind(TEXTURE_LOCATION);
        AbstractGui.blit(matrixStack, x + 1, y, 1, 0, width - 2, height, 32, 64);
        AbstractGui.blit(matrixStack, x + 1, y + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, width - 2, amount, 32, 64);
    }

    private int getScaled() {
        if (storage.getMaxEnergyStored() <= 0) {
            return height;
        }
        // avoid integer overflow here
        return (int)((long)storage.getEnergyStored() * height / storage.getMaxEnergyStored());
    }

    @Override
    public List<ITextComponent> getTooltip() {
        return Collections.singletonList(new StringTextComponent(MiscUtil.commify(storage.getEnergyStored()) + " / " + MiscUtil.commify(storage.getMaxEnergyStored()) + " FE"));
    }
}
