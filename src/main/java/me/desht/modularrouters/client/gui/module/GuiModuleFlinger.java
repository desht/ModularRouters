package me.desht.modularrouters.client.gui.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.textfield.FloatTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.item.module.FlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiModuleFlinger extends GuiModule {
    private FloatTextField speedField;
    private FloatTextField pitchField;
    private FloatTextField yawField;

    public GuiModuleFlinger(ContainerModule container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        addButton(new TooltipButton(0, guiLeft + 130, guiTop + 15, "speed", FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED));
        addButton(new TooltipButton(1, guiLeft + 130, guiTop + 33, "pitch", FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH));
        addButton(new TooltipButton(2, guiLeft + 130, guiTop + 51, "yaw", FlingerModule.MIN_YAW, FlingerModule.MAX_YAW));

        TextFieldManager manager = getOrCreateTextFieldManager();

        CompiledFlingerModule cfm = new CompiledFlingerModule(null, moduleItemStack);

        speedField = new FloatTextField(manager, font, guiLeft + 152, guiTop + 19, 35, 12,
                FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED);
        speedField.setPrecision(2);
        speedField.setValue(cfm.getSpeed());
        speedField.setResponder(str -> sendModuleSettingsDelayed(5));
        speedField.setIncr(0.1f, 0.5f, 10.0f);
        speedField.useGuiTextBackground();

        pitchField = new FloatTextField(manager, font, guiLeft + 152, guiTop + 37, 35, 12,
                FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH);
        pitchField.setValue(cfm.getPitch());
        pitchField.setResponder(str -> sendModuleSettingsDelayed(5));
        pitchField.useGuiTextBackground();

        yawField = new FloatTextField(manager, font, guiLeft + 152, guiTop + 55, 35, 12,
                FlingerModule.MIN_YAW, FlingerModule.MAX_YAW);
        yawField.setValue(cfm.getYaw());
        yawField.setResponder(str -> sendModuleSettingsDelayed(5));
        yawField.useGuiTextBackground();

        manager.focus(1);  // field 0 is the regulator amount textfield

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 13, guiLeft + 186, guiTop + 32, "guiText.popup.flinger.speed");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 31, guiLeft + 186, guiTop + 50, "guiText.popup.flinger.pitch");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 49, guiLeft + 186, guiTop + 68, "guiText.popup.flinger.yaw");
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.func_230450_a_(matrixStack, partialTicks, mouseX, mouseY);

        this.blit(matrixStack, guiLeft + 148, guiTop + 16, LARGE_TEXTFIELD_XY.x, LARGE_TEXTFIELD_XY.y, 35, 14);
        this.blit(matrixStack, guiLeft + 148, guiTop + 34, LARGE_TEXTFIELD_XY.x, LARGE_TEXTFIELD_XY.y, 35, 14);
        this.blit(matrixStack, guiLeft + 148, guiTop + 52, LARGE_TEXTFIELD_XY.x, LARGE_TEXTFIELD_XY.y, 35, 14);
    }

    @Override
    protected CompoundNBT buildMessageData() {
        CompoundNBT compound = super.buildMessageData();
        compound.putFloat(CompiledFlingerModule.NBT_SPEED, speedField.getValue());
        compound.putFloat(CompiledFlingerModule.NBT_PITCH, pitchField.getValue());
        compound.putFloat(CompiledFlingerModule.NBT_YAW, yawField.getValue());
        return compound;
    }

    private static class TooltipButton extends TexturedButton {
        private final int buttonId;

        TooltipButton(int buttonId, int x, int y, String key, float min, float max) {
            super(x, y, 16, 16, p -> {});
            this.buttonId = buttonId;
            tooltip1.add(new TranslationTextComponent("guiText.tooltip.flinger." + key, min, max));
            MiscUtil.appendMultilineText(tooltip1, TextFormatting.WHITE, "guiText.tooltip.numberFieldTooltip");
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        protected int getTextureX() {
            return 48 + 16 * buttonId;
        }

        @Override
        protected int getTextureY() {
            return 0;
        }

        @Override
        public void playDownSound(SoundHandler soundHandlerIn) {
            // no click sound
        }
    }
}
