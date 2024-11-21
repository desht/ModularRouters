package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule;
import me.desht.modularrouters.logic.settings.TransferDirection;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class FluidModuleScreen extends ModuleScreen {
    private static final ItemStack bucketStack = new ItemStack(Items.BUCKET);
    private static final ItemStack routerStack = new ItemStack(ModBlocks.MODULAR_ROUTER.get());
    private static final ItemStack waterStack = new ItemStack(Items.WATER_BUCKET);

    private ForceEmptyButton forceEmptyButton;
    private RegulateAbsoluteButton regulationTypeButton;
    private FluidDirectionButton fluidDirButton;
    private IntegerTextField maxTransferField;

    public FluidModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledFluidModule cfm = new CompiledFluidModule(null, moduleItemStack);

        int max = ConfigHolder.common.router.baseTickRate.get() * ConfigHolder.common.router.fluidMaxTransferRate.get();
        maxTransferField = new IntegerTextField(font, leftPos + 152, topPos + 23, 34, 12,
                Range.of(0, max));
        maxTransferField.setValue(cfm.getMaxTransfer());
        maxTransferField.setResponder(str -> sendModuleSettingsDelayed(5));
        maxTransferField.setIncr(100, 10, 10);
        maxTransferField.useGuiTextBackground();
        addRenderableWidget(maxTransferField);

        addRenderableWidget(new TooltipButton(leftPos + 130, topPos + 19, 16, 16, bucketStack));
        addRenderableWidget(fluidDirButton = new FluidDirectionButton(leftPos + 148, topPos + 44, cfm.getFluidDirection()));
        addRenderableWidget(forceEmptyButton = new ForceEmptyButton(leftPos + 168, topPos + 69, cfm.isForceEmpty()));
        addRenderableWidget(regulationTypeButton = new RegulateAbsoluteButton(regulatorTextField.getX() + regulatorTextField.getWidth() + 2, regulatorTextField.getY() - 1, 18, 14, b -> toggleRegulationType(), cfm.isRegulateAbsolute()));

        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 17, leftPos + 183, topPos + 35, "modularrouters.guiText.popup.fluid.maxTransfer");
        getMouseOverHelp().addHelpRegion(leftPos + 126, topPos + 42, leftPos + 185, topPos + 61, "modularrouters.guiText.popup.fluid.direction");
        getMouseOverHelp().addHelpRegion(leftPos + 128, topPos + 67, leftPos + 185, topPos + 86, "modularrouters.guiText.popup.fluid.forceEmpty");
    }

    @Override
    protected IntegerTextField buildRegulationTextField() {
        IntegerTextField tf = new IntegerTextField(font, leftPos + 128, topPos + 90, 40, 12, Range.of(0, Integer.MAX_VALUE));
        tf.setValue(getRegulatorAmount());
        tf.setResponder((str) -> {
            setRegulatorAmount(str.isEmpty() ? 0 : Integer.parseInt(str));
            sendModuleSettingsDelayed(5);
        });
        return tf;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        // text entry field custom background - super has already bound the correct texture
        graphics.blit(GUI_TEXTURE, leftPos + 146, topPos + 20, LARGE_TEXTFIELD_XY.x(), LARGE_TEXTFIELD_XY.y(), 35, 14);

        graphics.renderItem(routerStack, leftPos + 128, topPos + 44);
        graphics.renderItem(waterStack, leftPos + 168, topPos + 44);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        if (forceEmptyButton.visible) {
            MutableComponent c = xlate("modularrouters.guiText.label.fluidForceEmpty");
            graphics.drawString(font, c, 165 - font.width(c), 73, 0x202040, false);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        regulationTypeButton.visible = regulatorTextField.visible;
        regulationTypeButton.setText();
        regulatorTextField.setRange(Range.of(0, regulationTypeButton.regulateAbsolute ? Integer.MAX_VALUE : 100));
        forceEmptyButton.visible = fluidDirButton.getState() == TransferDirection.FROM_ROUTER;
    }

    @Override
    protected ItemStack buildModifiedItemStack() {
        return Util.make(super.buildModifiedItemStack(), stack ->
                stack.set(ModDataComponents.FLUID_SETTINGS, new CompiledFluidModule.FluidModuleSettings(
                        maxTransferField.getIntValue(),
                        fluidDirButton.getState(),
                        forceEmptyButton.isToggled(),
                        regulationTypeButton.regulateAbsolute
                ))
        );
    }

    private class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y, int width, int height, ItemStack renderStack) {
            super(x, y, width, height, renderStack, true, p -> {});
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(xlate("modularrouters.guiText.tooltip.fluidTransferTooltip"));
            tooltip.add(Component.empty());
            getItemRouter().ifPresent(router -> {
                int ftRate = router.getFluidTransferRate();
                int tickRate = router.getTickRate();
                tooltip.add(xlate("modularrouters.guiText.tooltip.maxFluidPerOp", ftRate * tickRate, tickRate, ftRate));
                tooltip.add(Component.empty().plainCopy());
            });
            tooltip.add(xlate("modularrouters.guiText.tooltip.numberFieldTooltip"));
            ClientUtil.setMultilineTooltip(this, tooltip);
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
            // no sound
        }
    }

    private void toggleRegulationType() {
        regulationTypeButton.toggle();
        regulatorTextField.setRange(regulationTypeButton.regulateAbsolute ? Range.of(0, Integer.MAX_VALUE) : Range.of(0, 100));
        sendToServer();
    }

    private class FluidDirectionButton extends TexturedCyclerButton<TransferDirection> {
        FluidDirectionButton(int x, int y, TransferDirection initialVal) {
            super(x, y, 16, 16, initialVal, FluidModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(160 + getState().ordinal() * 16, 16);
        }
    }

    private class ForceEmptyButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(112, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(192, 16);

        ForceEmptyButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, FluidModuleScreen.this);
            setTooltips(
                    Tooltip.create(xlate("modularrouters.guiText.tooltip.fluidForceEmpty.false")),
                    Tooltip.create(xlate("modularrouters.guiText.tooltip.fluidForceEmpty.true"))
            );
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
