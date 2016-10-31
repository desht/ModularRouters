package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.gui.widgets.IntegerTextField;
import me.desht.modularrouters.gui.widgets.ItemStackButton;
import me.desht.modularrouters.gui.widgets.TextFieldManager;
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

    public GuiModuleDetector(ModuleContainer containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleDetector(ModuleContainer containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledDetectorModule settings = new CompiledDetectorModule(null, moduleItemStack);
        signalStrength = settings.getSignalLevel();
        isStrong = settings.isStrongSignal();
    }

    @Override
    public void initGui() {
        super.initGui();

        TextFieldManager manager = getOrCreateTextFieldManager();

        IntegerTextField textBox = new IntegerTextField(manager, SIGNAL_LEVEL_TEXTFIELD_ID, fontRendererObj, guiLeft + 152, guiTop + 17, 20, 12, 0, 15);
        textBox.setValue(signalStrength);
        textBox.setGuiResponder(this);
        textBox.setIncr(1, 4);

        manager.focus(0);

        String label = I18n.format("itemText.misc.strongSignal." + isStrong);
        buttonList.add(new GuiButton(STRENGTH_BUTTON_ID, guiLeft + 138, guiTop + 33, 40, 20, label));

        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_ID, guiLeft + 132, guiTop + 15, 16, 16, redstoneStack));
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
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.intFieldTooltip");
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }
}
