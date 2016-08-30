package me.desht.modularrouters.gui;

import com.google.common.collect.Lists;
import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.gui.widgets.ItemStackButton;
import me.desht.modularrouters.gui.widgets.TextFieldWidget;
import me.desht.modularrouters.logic.CompiledDetectorModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.regex.Pattern;

public class GuiModuleDetector extends GuiModule implements GuiPageButtonList.GuiResponder {
    private static final Pattern INT_MATCHER = Pattern.compile("^[0-9]+$");
    private static final int SIGNAL_LEVEL_TEXTFIELD_ID = 0;
    private static final int STRENGTH_BUTTON_ID = 101;
    private static final int TOOLTIP_BUTTON_ID = 102;
    private static final ItemStack redstoneStack = new ItemStack(Items.REDSTONE);

    private SignalLevelField textBox;
    private int signalStrength;
    private boolean isStrong;

    private int sendTimeout = 0;

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

        textBox = new SignalLevelField(this, SIGNAL_LEVEL_TEXTFIELD_ID, fontRendererObj, guiLeft + 152, guiTop + 17, 20, 12);
        textBox.setText(Integer.toString(signalStrength));
        textBox.setGuiResponder(this);
        addTextField(textBox);

        focus(0);

        String label = I18n.format("itemText.misc.strongSignal" + isStrong);
        buttonList.add(new GuiButton(STRENGTH_BUTTON_ID, guiLeft + 138, guiTop + 33, 40, 20, label));

        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_ID, guiLeft + 132, guiTop + 15, 16, 16, redstoneStack));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == STRENGTH_BUTTON_ID) {
            isStrong = !isStrong;
            button.displayString = I18n.format("itemText.misc.strongSignal" + isStrong);
            sendModuleSettingsToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    public void setEntryValue(int id, boolean value) { }

    @Override
    public void setEntryValue(int id, float value) { }

    @Override
    public void setEntryValue(int id, String value) {
        if (id == SIGNAL_LEVEL_TEXTFIELD_ID) {
            signalStrength = value.isEmpty() ? 0 : Integer.parseInt(value);
            sendModuleSettingsDelayed(5);
        }
    }

    @Override
    protected NBTTagCompound getExtMessageData() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setByte(CompiledDetectorModule.NBT_SIGNAL_LEVEL, (byte) signalStrength);
        compound.setBoolean(CompiledDetectorModule.NBT_STRONG_SIGNAL, isStrong);
        return compound;
    }

    private static class TooltipButton extends ItemStackButton {
        private static List<String> res;
        public TooltipButton(int buttonId, int x, int y, int width, int height, ItemStack renderStack) {
            super(buttonId, x, y, width, height, renderStack, true);
        }

        @Override
        public List<String> getTooltip() {
            if (res == null) {
                res = Lists.newArrayList();
                MiscUtil.appendMultiline(res, I18n.format("guiText.tooltip.detectorHint"));
            }
            return res;
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }

    private class SignalLevelField extends TextFieldWidget {
        SignalLevelField(GuiContainerBase parent, int id, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
            super(parent, id, fontrendererObj, x, y, par5Width, par6Height);
            setMaxStringLength(2);
            setValidator(input -> {
                if (input == null || input.isEmpty()) {
                    return true;  // treat as numeric zero
                }
                if (!INT_MATCHER.matcher(input).matches()) {
                    return false;
                }
                int n = Integer.parseInt(input);
                return n >= 0 && n <= 15;
            });
        }

        @Override
        public boolean textboxKeyTyped(char typedChar, int keyCode) {
            switch (keyCode) {
                case Keyboard.KEY_UP:
                    return adjustField(1);
                case Keyboard.KEY_DOWN:
                    return adjustField(-1);
                case Keyboard.KEY_PRIOR:
                    return adjustField(15);
                case Keyboard.KEY_NEXT:
                    return adjustField(-15);
                default:
                    return super.textboxKeyTyped(typedChar, keyCode);
            }
        }

        @Override
        public void onMouseWheel(int direction) {
            adjustField(direction);
        }

        public boolean adjustField(int adj) {
            int val = Integer.parseInt(getText());
            setText("");
            writeText(Integer.toString(Math.max(0, Math.min(15, val + adj))));
            return true;
        }
    }
}
