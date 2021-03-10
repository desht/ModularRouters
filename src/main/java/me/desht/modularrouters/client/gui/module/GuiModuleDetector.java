package me.desht.modularrouters.client.gui.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.util.ClientUtil;
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
import org.apache.commons.lang3.Range;

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

        intField = new IntegerTextField(manager, font, leftPos + 152, topPos + 19, 20, 12, Range.between(0, 15));
        intField.setValue(cdm.getSignalLevel());
        intField.setResponder((str) -> sendModuleSettingsDelayed(5));
        intField.setIncr(1, 4);
        intField.useGuiTextBackground();

        manager.focus(0);

        ITextComponent label = ClientUtil.xlate("modularrouters.itemText.misc.strongSignal." + cdm.isStrongSignal());
        isStrong = cdm.isStrongSignal();
        addButton(new Button(leftPos + 138, topPos + 33, 40, 20, label, button -> {
            isStrong = !isStrong;
            button.setMessage(ClientUtil.xlate("modularrouters.itemText.misc.strongSignal." + isStrong));
            GuiModuleDetector.this.sendToServer();
        }));

        addButton(new TooltipButton(leftPos + 132, topPos + 15, 16, 16, redstoneStack));

        getMouseOverHelp().addHelpRegion(leftPos + 129, topPos + 14, leftPos + 172, topPos + 31, "modularrouters.guiText.popup.detector.signalLevel");
        getMouseOverHelp().addHelpRegion(leftPos + 135, topPos + 31, leftPos + 180, topPos + 54, "modularrouters.guiText.popup.detector.weakStrong");
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        // text entry field background - super has already bound the correct texture
        this.blit(matrixStack, leftPos + 148, topPos + 16, SMALL_TEXTFIELD_XY.x, SMALL_TEXTFIELD_XY.y, 21, 14);
    }

    @Override
    protected CompoundNBT buildMessageData() {
        CompoundNBT compound = super.buildMessageData();
        compound.putByte(CompiledDetectorModule.NBT_SIGNAL_LEVEL, (byte) intField.getIntValue());
        compound.putBoolean(CompiledDetectorModule.NBT_STRONG_SIGNAL, isStrong);
        return compound;
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y, int width, int height, ItemStack renderStack) {
            super(x, y, width, height, renderStack, true, p -> {});
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "modularrouters.guiText.tooltip.detectorTooltip");
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "modularrouters.guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playDownSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }
}
