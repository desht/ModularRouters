package me.desht.modularrouters.client.gui.module;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.Range;

public class DetectorModuleScreen extends AbstractModuleScreen {
    private static final ItemStack redstoneStack = new ItemStack(Items.REDSTONE);

    private boolean isStrong;
    private IntegerTextField intField;

    public DetectorModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
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

        Component label = ClientUtil.xlate("modularrouters.itemText.misc.strongSignal." + cdm.isStrongSignal());
        isStrong = cdm.isStrongSignal();
        addRenderableWidget(new Button(leftPos + 138, topPos + 33, 40, 20, label, button -> {
            isStrong = !isStrong;
            button.setMessage(ClientUtil.xlate("modularrouters.itemText.misc.strongSignal." + isStrong));
            DetectorModuleScreen.this.sendToServer();
        }));

        addRenderableWidget(new TooltipButton(leftPos + 132, topPos + 15, 16, 16, redstoneStack));

        getMouseOverHelp().addHelpRegion(leftPos + 129, topPos + 14, leftPos + 172, topPos + 31, "modularrouters.guiText.popup.detector.signalLevel");
        getMouseOverHelp().addHelpRegion(leftPos + 135, topPos + 31, leftPos + 180, topPos + 54, "modularrouters.guiText.popup.detector.weakStrong");
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        // text entry field background - super has already bound the correct texture
        this.blit(matrixStack, leftPos + 148, topPos + 16, SMALL_TEXTFIELD_XY.x(), SMALL_TEXTFIELD_XY.y(), 21, 14);
    }

    @Override
    protected CompoundTag buildMessageData() {
        CompoundTag compound = super.buildMessageData();
        compound.putByte(CompiledDetectorModule.NBT_SIGNAL_LEVEL, (byte) intField.getIntValue());
        compound.putBoolean(CompiledDetectorModule.NBT_STRONG_SIGNAL, isStrong);
        return compound;
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y, int width, int height, ItemStack renderStack) {
            super(x, y, width, height, renderStack, true, p -> {});
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.detectorTooltip");
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
            // no sound
        }
    }
}
