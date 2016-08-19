package me.desht.modularrouters.gui;

import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.logic.CompiledDetectorModuleSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.regex.Pattern;

public class GuiModuleDetector extends GuiModule implements GuiPageButtonList.GuiResponder {
    private static final Pattern INT_MATCHER = Pattern.compile("^[0-9]+$");
    private static final int SIGNAL_LEVEL_TEXTFIELD_ID = 100;
    private static final int STRENGTH_BUTTON_ID = 101;

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

        CompiledDetectorModuleSettings settings = new CompiledDetectorModuleSettings(moduleItemStack);
        signalStrength = settings.getSignalLevel();
        isStrong = settings.isStrongSignal();
    }

    @Override
    public void initGui() {
        super.initGui();

        textBox = new SignalLevelField(SIGNAL_LEVEL_TEXTFIELD_ID, fontRendererObj, guiLeft + 144, guiTop + 17, 20, 12);
        textBox.setText(Integer.toString(signalStrength));
        textBox.setGuiResponder(this);

        String label = I18n.format("itemText.misc.strongSignal" + isStrong);
        buttonList.add(new GuiButton(STRENGTH_BUTTON_ID, guiLeft + 130, guiTop + 33, 40, 20, label));
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
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        RenderHelper.renderItemStack(Minecraft.getMinecraft(), redstoneStack, guiLeft + 128, guiTop + 15, "");
        textBox.drawTextBox();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (sendTimeout > 0) {
            sendTimeout--;
            if (sendTimeout <= 0) {
                sendModuleSettingsToServer();
            }
        }
        if (textBox.isFocused()) {
            textBox.updateCursorCounter();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (textBox.isFocused() && wheel != 0) {
            textBox.adjustField(wheel < 0 ? -1 : 1);
        } else {
            super.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        super.mouseClicked(x, y, btn);
        textBox.mouseClicked(x, y, btn);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (textBox.isFocused()) {
            textBox.textboxKeyTyped(typedChar, keyCode);
        } else if (keyCode == Keyboard.KEY_TAB) {
            textBox.setFocused(true);
        }
    }

    @Override
    public void setEntryValue(int id, boolean value) {

    }

    @Override
    public void setEntryValue(int id, float value) {

    }

    @Override
    public void setEntryValue(int id, String value) {
        if (id == SIGNAL_LEVEL_TEXTFIELD_ID) {
            signalStrength = value.isEmpty() ? 0 : Integer.parseInt(value);
            sendTimeout = 5;  // delay sending by 5 ticks to minimize sending partial updates
        }
    }

    @Override
    public void onGuiClosed() {
        if (sendTimeout > 0) {
            sendModuleSettingsToServer();
        }
        super.onGuiClosed();
    }

    @Override
    protected NBTTagCompound getExtMessageData() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setByte(CompiledDetectorModuleSettings.NBT_SIGNAL_LEVEL, (byte) signalStrength);
        compound.setBoolean(CompiledDetectorModuleSettings.NBT_STRONG_SIGNAL, isStrong);
        return compound;
    }

    private class SignalLevelField extends GuiTextField {
        SignalLevelField(int id, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
            super(id, fontrendererObj, x, y, par5Width, par6Height);
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
            setFocused(true);
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

        public boolean adjustField(int adj) {
            int val = Integer.parseInt(getText());
            setText("");
            writeText(Integer.toString(Math.max(0, Math.min(15, val + adj))));
            return true;
        }
    }
}
