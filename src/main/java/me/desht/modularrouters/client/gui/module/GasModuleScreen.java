package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.item.module.GasModule1;
import me.desht.modularrouters.logic.compiled.CompiledGasModule1;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.apache.commons.lang3.Range;

import java.util.Collections;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class GasModuleScreen extends AbstractModuleScreen {
    private static final ItemStack bucketStack = new ItemStack(Items.BUCKET);
    private static final ItemStack routerStack = new ItemStack(ModBlocks.MODULAR_ROUTER.get());
    private static final ItemStack waterStack = new ItemStack(Items.BUCKET);

    private ForceEmptyButton forceEmptyButton;
    private RegulateAbsoluteButton regulationTypeButton;
    private GasDirectionButton gasDirButton;
    private IntegerTextField maxTransferField;

    public GasModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledGasModule1 cfm = new CompiledGasModule1(null, moduleItemStack);

        TextFieldManager manager = getOrCreateTextFieldManager();

        int max = ConfigHolder.common.router.baseTickRate.get() * ConfigHolder.common.router.gasMaxTransferRate.get();
        maxTransferField = new IntegerTextField(manager, font, leftPos + 152, topPos + 23, 34, 12,
                Range.between(0, max));
        maxTransferField.setValue(cfm.getMaxTransfer());
        maxTransferField.setResponder(str -> sendModuleSettingsDelayed(5));
        maxTransferField.setIncr(100, 10, 10);
        maxTransferField.useGuiTextBackground();
        manager.focus(0);

        addRenderableWidget(new TooltipButton(leftPos + 130, topPos + 19, 16, 16, bucketStack));
        addRenderableWidget(gasDirButton = new GasDirectionButton(leftPos + 148, topPos + 44, cfm.getGasDirection()));
        addRenderableWidget(forceEmptyButton = new ForceEmptyButton(leftPos + 168, topPos + 69, cfm.isForceEmpty()));
        addRenderableWidget(regulationTypeButton = new RegulateAbsoluteButton(regulatorTextField.x + regulatorTextField.getWidth() + 2, regulatorTextField.y - 1, 18, 14, b -> toggleRegulationType(), cfm.isRegulateAbsolute()));

        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 17, leftPos + 183, topPos + 35, "modularrouters.guiText.popup.gas.maxTransfer");
        getMouseOverHelp().addHelpRegion(leftPos + 126, topPos + 42, leftPos + 185, topPos + 61, "modularrouters.guiText.popup.gas.direction");
        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 67, leftPos + 185, topPos + 86, "modularrouters.guiText.popup.gas.forceEmpty");
    }

    @Override
    protected IntegerTextField buildRegulationTextField(TextFieldManager manager) {
        IntegerTextField tf = new IntegerTextField(manager, font, leftPos + 128, topPos + 90, 40, 12, Range.between(0, Integer.MAX_VALUE));
        tf.setValue(getRegulatorAmount());
        tf.setResponder((str) -> {
            setRegulatorAmount(str.isEmpty() ? 0 : Integer.parseInt(str));
            sendModuleSettingsDelayed(5);
        });
        return tf;
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        // text entry field custom background - super has already bound the correct texture
        this.blit(matrixStack, leftPos + 146, topPos + 20, LARGE_TEXTFIELD_XY.x(), LARGE_TEXTFIELD_XY.y(), 35, 14);

        GuiUtil.renderItemStack(matrixStack, minecraft, routerStack, leftPos + 128, topPos + 44, "");
        GuiUtil.renderItemStack(matrixStack, minecraft, waterStack, leftPos + 168, topPos + 44, "");
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        if (forceEmptyButton.visible) {
            MutableComponent c = xlate("modularrouters.guiText.label.gasForceEmpty");
            font.draw(matrixStack, c, 165 - font.width(c), 73, 0x202040);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        regulationTypeButton.visible = regulatorTextField.visible;
        regulationTypeButton.setText();
        regulatorTextField.setRange(Range.between(0, regulationTypeButton.regulateAbsolute ? Integer.MAX_VALUE : 100));
        forceEmptyButton.visible = gasDirButton.getState() == GasModule1.GasDirection.OUT;
    }

    @Override
    protected CompoundTag buildMessageData() {
        CompoundTag compound = super.buildMessageData();
        compound.putInt(CompiledGasModule1.NBT_MAX_TRANSFER, maxTransferField.getIntValue());
        compound.putByte(CompiledGasModule1.NBT_GAS_DIRECTION, (byte) gasDirButton.getState().ordinal());
        compound.putBoolean(CompiledGasModule1.NBT_FORCE_EMPTY, forceEmptyButton.isToggled());
        compound.putBoolean(CompiledGasModule1.NBT_REGULATE_ABSOLUTE, regulationTypeButton.regulateAbsolute);
        return compound;
    }

    private class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y, int width, int height, ItemStack renderStack) {
            super(x, y, width, height, renderStack, true, p -> {});
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.gasTransferTooltip");
            tooltip1.add(Component.empty().plainCopy());
            getItemRouter().ifPresent(router -> {
                int ftRate = router.getGasTransferRate();
                int tickRate = router.getTickRate();
                tooltip1.add(xlate("modularrouters.guiText.tooltip.maxGasPerOp", ftRate * tickRate, tickRate, ftRate));
                tooltip1.add(Component.empty().plainCopy());
            });
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
            // no sound
        }
    }

    private void toggleRegulationType() {
        regulationTypeButton.toggle();
        regulatorTextField.setRange(regulationTypeButton.regulateAbsolute ? Range.between(0, Integer.MAX_VALUE) : Range.between(0, 100));
        sendToServer();
    }

    private class GasDirectionButton extends TexturedCyclerButton<GasModule1.GasDirection> {
        private final List<List<Component>> tooltips = Lists.newArrayList();

        GasDirectionButton(int x, int y, GasModule1.GasDirection initialVal) {
            super(x, y, 16, 16, initialVal, GasModuleScreen.this);
            for (GasModule1.GasDirection dir : GasModule1.GasDirection.values()) {
                tooltips.add(Collections.singletonList(xlate(dir.getTranslationKey())));
            }
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(160 + getState().ordinal() * 16, 16);
        }

        @Override
        public List<Component> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }

    private class ForceEmptyButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(112, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(192, 16);

        ForceEmptyButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, GasModuleScreen.this);
            MiscUtil.appendMultilineText(tooltip1, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.gasForceEmpty.false");
            MiscUtil.appendMultilineText(tooltip2, ChatFormatting.WHITE, "modularrouters.guiText.tooltip.gasForceEmpty.true");
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }

    private static class RegulateAbsoluteButton extends ExtendedButton {
        private boolean regulateAbsolute;

        public RegulateAbsoluteButton(int xPos, int yPos, int width, int height, OnPress pressable, boolean regulateAbsolute) {
            super(xPos, yPos, width, height, Component.empty(), pressable);
            this.regulateAbsolute = regulateAbsolute;
        }

        private void toggle() {
            regulateAbsolute = !regulateAbsolute;
        }

        void setText() {
            setMessage(Component.literal(regulateAbsolute ? "mB" : "%"));
        }
    }
}
