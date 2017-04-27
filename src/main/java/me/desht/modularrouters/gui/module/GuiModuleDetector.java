package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class GuiModuleDetector extends GuiModule {
    private static final int STRENGTH_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;
    private static final int TOOLTIP_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 1;
    private static final int SIGNAL_LEVEL_TEXTFIELD_ID = GuiModule.EXTRA_TEXTFIELD_BASE;

    private static final ItemStack redstoneStack = new ItemStack(Items.REDSTONE);

    private int signalStrength;
    private boolean isStrong;

    public GuiModuleDetector(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleDetector(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledDetectorModule settings = new CompiledDetectorModule(null, moduleItemStack);
        signalStrength = settings.getSignalLevel();
        isStrong = settings.isStrongSignal();
    }

    @Override
    public void initGui() {
        super.initGui();

        TextFieldManager manager = getOrCreateTextFieldManager();

        IntegerTextField intField = new IntegerTextField(manager, SIGNAL_LEVEL_TEXTFIELD_ID, fontRenderer, guiLeft + 152, guiTop + 19, 20, 12, 0, 15);
        intField.setValue(signalStrength);
        intField.setGuiResponder(this);
        intField.setIncr(1, 4);
        intField.useGuiTextBackground();

        manager.focus(0);

        String label = I18n.format("itemText.misc.strongSignal." + isStrong);
        buttonList.add(new GuiButton(STRENGTH_BUTTON_ID, guiLeft + 138, guiTop + 33, 40, 20, label));

        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_ID, guiLeft + 132, guiTop + 15, 16, 16, redstoneStack));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        // super has already bound the correct texture
        this.drawTexturedModalRect(guiLeft + 148, guiTop + 16, 0, 182, 21, 14);  // text entry field custom background
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == STRENGTH_BUTTON_ID) {
            isStrong = !isStrong;
            button.displayString = I18n.format("itemText.misc.strongSignal." + isStrong);
            sendModuleSettingsToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    public void setEntryValue(int id, String value) {
        if (id == SIGNAL_LEVEL_TEXTFIELD_ID) {
            signalStrength = value.isEmpty() ? 0 : Integer.parseInt(value);
            sendModuleSettingsDelayed(5);
        } else {
            super.setEntryValue(id, value);
        }
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.setByte(CompiledDetectorModule.NBT_SIGNAL_LEVEL, (byte) signalStrength);
        compound.setBoolean(CompiledDetectorModule.NBT_STRONG_SIGNAL, isStrong);
        return compound;
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int buttonId, int x, int y, int width, int height, ItemStack renderStack) {
            super(buttonId, x, y, width, height, renderStack, true);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.detectorTooltip");
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }
}
