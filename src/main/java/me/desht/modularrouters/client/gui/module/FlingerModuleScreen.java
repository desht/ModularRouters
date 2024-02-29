package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.textfield.FloatTextField;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.item.module.FlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class FlingerModuleScreen extends AbstractModuleScreen {
    private FloatTextField speedField;
    private FloatTextField pitchField;
    private FloatTextField yawField;

    public FlingerModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new TooltipButton(0, leftPos + 130, topPos + 15, "speed", FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED));
        addRenderableWidget(new TooltipButton(1, leftPos + 130, topPos + 33, "pitch", FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH));
        addRenderableWidget(new TooltipButton(2, leftPos + 130, topPos + 51, "yaw", FlingerModule.MIN_YAW, FlingerModule.MAX_YAW));

//        TextFieldManager manager = getOrCreateTextFieldManager();

        CompiledFlingerModule cfm = new CompiledFlingerModule(null, moduleItemStack);

        speedField = new FloatTextField(font, leftPos + 152, topPos + 19, 35, 12,
                FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED);
        speedField.setPrecision(2);
        speedField.setValue(cfm.getSpeed());
        speedField.setResponder(str -> sendModuleSettingsDelayed(5));
        speedField.setIncr(0.1f, 0.5f, 10.0f);
        speedField.useGuiTextBackground();

        pitchField = new FloatTextField(font, leftPos + 152, topPos + 37, 35, 12,
                FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH);
        pitchField.setValue(cfm.getPitch());
        pitchField.setResponder(str -> sendModuleSettingsDelayed(5));
        pitchField.useGuiTextBackground();

        yawField = new FloatTextField(font, leftPos + 152, topPos + 55, 35, 12,
                FlingerModule.MIN_YAW, FlingerModule.MAX_YAW);
        yawField.setValue(cfm.getYaw());
        yawField.setResponder(str -> sendModuleSettingsDelayed(5));
        yawField.useGuiTextBackground();

        addRenderableWidget(speedField);
        addRenderableWidget(pitchField);
        addRenderableWidget(yawField);

//        manager.focus(1);  // field 0 is the regulator amount textfield

        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 13, leftPos + 186, topPos + 32, "modularrouters.guiText.popup.flinger.speed");
        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 31, leftPos + 186, topPos + 50, "modularrouters.guiText.popup.flinger.pitch");
        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 49, leftPos + 186, topPos + 68, "modularrouters.guiText.popup.flinger.yaw");
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        graphics.blit(GUI_TEXTURE, leftPos + 148, topPos + 16, LARGE_TEXTFIELD_XY.x(), LARGE_TEXTFIELD_XY.y(), 35, 14);
        graphics.blit(GUI_TEXTURE, leftPos + 148, topPos + 34, LARGE_TEXTFIELD_XY.x(), LARGE_TEXTFIELD_XY.y(), 35, 14);
        graphics.blit(GUI_TEXTURE, leftPos + 148, topPos + 52, LARGE_TEXTFIELD_XY.x(), LARGE_TEXTFIELD_XY.y(), 35, 14);
    }

    @Override
    protected CompoundTag buildMessageData() {
        return Util.make(super.buildMessageData(), tag -> {
            tag.putFloat(CompiledFlingerModule.NBT_SPEED, speedField.getFloatValue());
            tag.putFloat(CompiledFlingerModule.NBT_PITCH, pitchField.getFloatValue());
            tag.putFloat(CompiledFlingerModule.NBT_YAW, yawField.getFloatValue());
        });
    }

    private static class TooltipButton extends TexturedButton {
        private final int buttonId;

        TooltipButton(int buttonId, int x, int y, String key, float min, float max) {
            super(x, y, 16, 16, p -> {});
            this.buttonId = buttonId;
            ClientUtil.setMultilineTooltip(this,
                    xlate("modularrouters.guiText.tooltip.flinger." + key, min, max).withStyle(ChatFormatting.AQUA),
                    xlate("modularrouters.guiText.tooltip.numberFieldTooltip")
            );
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(48 + 16 * buttonId, 0);
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
            // no click sound
        }
    }
}
