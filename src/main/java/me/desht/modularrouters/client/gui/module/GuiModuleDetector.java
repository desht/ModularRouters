package me.desht.modularrouters.client.gui.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiModuleDetector extends GuiModule {
    private static final ItemStack redstoneStack = new ItemStack(Items.REDSTONE);

    private boolean isStrong;
    private IntegerTextField intField;

    public GuiModuleDetector(ContainerModule container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledDetectorModule cdm = new CompiledDetectorModule(null, moduleItemStack);

        TextFieldManager manager = getOrCreateTextFieldManager();

        intField = new IntegerTextField(manager, font, guiLeft + 152, guiTop + 19, 20, 12, 0, 15);
        intField.setValue(cdm.getSignalLevel());
        intField.setResponder((str) -> sendModuleSettingsDelayed(5));
        intField.setIncr(1, 4);
        intField.useGuiTextBackground();

        manager.focus(0);

        ITextComponent label = new TranslationTextComponent("itemText.misc.strongSignal." + cdm.isStrongSignal());
        isStrong = cdm.isStrongSignal();
        addButton(new Button(guiLeft + 138, guiTop + 33, 40, 20, label, button -> {
            isStrong = !isStrong;
            button.setMessage(new TranslationTextComponent("itemText.misc.strongSignal." + isStrong));
            GuiModuleDetector.this.sendToServer();
        }));

        addButton(new TooltipButton(guiLeft + 132, guiTop + 15, 16, 16, redstoneStack));

        getMouseOverHelp().addHelpRegion(guiLeft + 129, guiTop + 14, guiLeft + 172, guiTop + 31, "guiText.popup.detector.signalLevel");
        getMouseOverHelp().addHelpRegion(guiLeft + 135, guiTop + 31, guiLeft + 180, guiTop + 54, "guiText.popup.detector.weakStrong");
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.func_230450_a_(matrixStack, partialTicks, mouseX, mouseY);
        // text entry field background - super has already bound the correct texture
        this.blit(matrixStack, guiLeft + 148, guiTop + 16, SMALL_TEXTFIELD_XY.x, SMALL_TEXTFIELD_XY.y, 21, 14);
    }

    @Override
    protected CompoundNBT buildMessageData() {
        CompoundNBT compound = super.buildMessageData();
        compound.putByte(CompiledDetectorModule.NBT_SIGNAL_LEVEL, (byte) intField.getValue());
        compound.putBoolean(CompiledDetectorModule.NBT_STRONG_SIGNAL, isStrong);
        return compound;
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y, int width, int height, ItemStack renderStack) {
            super(x, y, width, height, renderStack, true, p -> {});
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "guiText.tooltip.detectorTooltip");
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playDownSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }
}
