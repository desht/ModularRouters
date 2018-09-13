package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.RenderHelper;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.module.FluidModule.FluidDirection;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

public class GuiModuleFluid extends GuiModule {
    private static final ItemStack bucketStack = new ItemStack(Items.BUCKET);
    private static final ItemStack routerStack = new ItemStack(RegistrarMR.ITEM_ROUTER);
    private static final ItemStack waterStack = new ItemStack(Items.WATER_BUCKET);

    private static final int TRANSFER_TEXTFIELD_ID = 1;
    private static final int TOOLTIP_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;
    private static final int FLUID_DIRECTION_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 1;
    private static final int FORCE_EMPTY_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 2;

    private ForceEmptyButton forceEmptyButton;

    private FluidDirection fluidDirection;
    private int maxTransfer;
    private boolean forceEmpty;

    public GuiModuleFluid(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleFluid(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledFluidModule cfm = new CompiledFluidModule(null, moduleItemStack);
        fluidDirection = cfm.getFluidDirection();
        maxTransfer = cfm.getMaxTransfer();
        forceEmpty = cfm.isForceEmpty();
    }

    @Override
    public void initGui() {
        super.initGui();

        TextFieldManager manager = getOrCreateTextFieldManager();

        int max = ConfigHandler.router.baseTickRate * ConfigHandler.router.fluidMaxTransferRate;
        IntegerTextField intField = new IntegerTextField(manager, TRANSFER_TEXTFIELD_ID, fontRenderer, guiLeft + 152, guiTop + 23, 34, 12, 0, max);
        intField.setValue(maxTransfer);
        intField.setGuiResponder(this);
        intField.setIncr(100, 10, 10);
        intField.useGuiTextBackground();
        manager.focus(0);

        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_ID, guiLeft + 130, guiTop + 19, 16, 16, bucketStack));
        buttonList.add(new FluidDirectionButton(FLUID_DIRECTION_BUTTON_ID, guiLeft + 148, guiTop + 44, fluidDirection));
        forceEmptyButton = new ForceEmptyButton(FORCE_EMPTY_BUTTON_ID, guiLeft + 168, guiTop + 69, forceEmpty);
        buttonList.add(forceEmptyButton);

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 17, guiLeft + 183, guiTop + 35, "guiText.popup.fluid.maxTransfer");
        getMouseOverHelp().addHelpRegion(guiLeft + 126, guiTop + 42, guiLeft + 185, guiTop + 61, "guiText.popup.fluid.direction");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 67, guiLeft + 185, guiTop + 86, "guiText.popup.fluid.forceEmpty");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        // text entry field custom background - super has already bound the correct texture
        this.drawTexturedModalRect(guiLeft + 146, guiTop + 20, LARGE_TEXTFIELD_XY.x, LARGE_TEXTFIELD_XY.y, 35, 14);

        RenderHelper.renderItemStack(mc, routerStack, guiLeft + 128, guiTop + 44, "");
        RenderHelper.renderItemStack(mc, waterStack, guiLeft + 168, guiTop + 44, "");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        if (regulatorTextField.getVisible()) {
            mc.fontRenderer.drawString("%", 179, 77, 0x404040);
        }
        if (fluidDirection == FluidDirection.OUT) {
            String s = I18n.format("guiText.label.fluidForceEmpty");
            mc.fontRenderer.drawString(s, 165 - mc.fontRenderer.getStringWidth(s), 73, 0x202040);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        forceEmptyButton.visible = fluidDirection == FluidDirection.OUT;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case FLUID_DIRECTION_BUTTON_ID:
                FluidDirectionButton fb = (FluidDirectionButton) button;
                fb.cycle(true);
                fluidDirection = fb.getState();
                sendModuleSettingsToServer();
                break;
            case FORCE_EMPTY_BUTTON_ID:
                ForceEmptyButton forceButton = (ForceEmptyButton) button;
                forceButton.toggle();
                forceEmpty = forceButton.isToggled();
                sendModuleSettingsToServer();
                break;
            default:
                super.actionPerformed(button);
        }
    }

    @Override
    public void setEntryValue(int id, String value) {
        switch (id) {
            case TRANSFER_TEXTFIELD_ID:
                maxTransfer = value.isEmpty() ? 0 : Integer.parseInt(value);
                sendModuleSettingsDelayed(5);
                break;
            default:
                super.setEntryValue(id, value);
        }
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.setInteger(CompiledFluidModule.NBT_MAX_TRANSFER, maxTransfer);
        compound.setByte(CompiledFluidModule.NBT_FLUID_DIRECTION, (byte) fluidDirection.ordinal());
        compound.setBoolean(CompiledFluidModule.NBT_FORCE_EMPTY, forceEmpty);
        return compound;
    }

    private class TooltipButton extends ItemStackButton {
        TooltipButton(int buttonId, int x, int y, int width, int height, ItemStack renderStack) {
            super(buttonId, x, y, width, height, renderStack, true);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.fluidTransferTooltip");
            tooltip1.add("");
            TileEntityItemRouter router = getItemRouterTE();
            if (router != null) {
                int ftRate = router.getFluidTransferRate();
                int tickRate = router.getTickRate();
                tooltip1.add(TextFormatting.GRAY + I18n.format("guiText.tooltip.maxFluidPerOp", ftRate * tickRate, tickRate, ftRate));
            }
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }

    private static class FluidDirectionButton extends TexturedCyclerButton<FluidDirection> {
        private final List<List<String>> tooltips = Lists.newArrayList();

        FluidDirectionButton(int buttonId, int x, int y, FluidDirection initialVal) {
            super(buttonId, x, y, 16, 16, initialVal);
            for (FluidDirection dir : FluidDirection.values()) {
                tooltips.add(Collections.singletonList(I18n.format("itemText.fluid.direction." + dir)));
            }
        }

        @Override
        protected int getTextureX() {
            return 160 + getState().ordinal() * 16;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }

        @Override
        public java.util.List<String> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }

    private static class ForceEmptyButton extends TexturedToggleButton {
        ForceEmptyButton(int buttonId, int x, int y, boolean initialVal) {
            super(buttonId, x, y, 16, 16, initialVal);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.fluidForceEmpty.false");
            MiscUtil.appendMultiline(tooltip2, "guiText.tooltip.fluidForceEmpty.true");
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 192 : 112;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }
}
