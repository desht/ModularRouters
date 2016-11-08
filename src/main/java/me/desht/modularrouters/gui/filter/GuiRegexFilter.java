package me.desht.modularrouters.gui.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.gui.BackButton;
import me.desht.modularrouters.gui.widgets.TextFieldManager;
import me.desht.modularrouters.gui.widgets.TextFieldWidget;
import me.desht.modularrouters.item.smartfilter.RegexFilter;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.sound.MRSoundEvents;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GuiRegexFilter extends GuiFilterScreen {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/regexfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 186;

    private static final int ADD_REGEX_ID = 1;
    private static final int BACK_BUTTON_ID = 2;
    private static final int BASE_REMOVE_ID = 100;

    private int xPos, yPos;
    private RegexTextField regexTextField;
    private String errorMsg = "";
    private int errorTimer = 60;  // 3 seconds

    private final List<String> regexList = Lists.newArrayList();

    public GuiRegexFilter(ItemStack filterStack, BlockPos routerPos, Integer moduleSlotIndex, Integer filterSlotIndex, EnumHand hand) {
        super(filterStack, routerPos, moduleSlotIndex, filterSlotIndex, hand);

        regexList.addAll(RegexFilter.getRegexList(filterStack));
    }

    @Override
    public void initGui() {
        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        TextFieldManager manager = getTextFieldManager().clear();
        regexTextField = new RegexTextField(this, 1, fontRendererObj, xPos + 8, yPos + 22, 144, 18);

        manager.focus(0);

        buttonList.clear();

        if (filterSlotIndex >= 0) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, xPos - 12, yPos));
        }
        buttonList.add(new Buttons.AddButton(ADD_REGEX_ID, xPos + 156, yPos + 23));
        for (int i = 0; i < regexList.size(); i++) {
            buttonList.add(new Buttons.DeleteButton(BASE_REMOVE_ID + i, xPos + 8, yPos + 52 + i * 19));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        fontRendererObj.drawString(title, xPos + GUI_WIDTH / 2 - this.fontRendererObj.getStringWidth(title) / 2, yPos + 6, 0x404040);

        for (int i = 0; i < regexList.size(); i++) {
            String regex = regexList.get(i);
            fontRendererObj.drawString("/" + regex + "/", xPos + 28, yPos + 55 + i * 19, 0x404080);
        }

        if (!errorMsg.isEmpty()) {
            fontRendererObj.drawString(errorMsg, xPos + 8, yPos + 170, 0x804040);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        if (errorTimer > 0) {
            if (--errorTimer == 0) {
                errorMsg = "";
            }
        }
        super.updateScreen();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ADD_REGEX_ID && !regexTextField.getText().isEmpty()) {
            addRegex();
        } else if (button.id >= BASE_REMOVE_ID && button.id < BASE_REMOVE_ID + regexList.size()) {
            removeRegexAt(button.id - BASE_REMOVE_ID);
        } else if (button.id == BACK_BUTTON_ID) {
            closeGUI();
        } else {
            super.actionPerformed(button);
        }
    }

    private void removeRegexAt(int pos) {
        NBTTagCompound ext = new NBTTagCompound();
        ext.setInteger("Pos", pos);
        if (routerPos != null) {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.REMOVE_AT, routerPos, moduleSlotIndex, filterSlotIndex, ext));
        } else {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.REMOVE_AT, hand, filterSlotIndex, ext));
        }
        errorMsg = "";
    }

    private void addRegex() {
        NBTTagCompound ext = new NBTTagCompound();
        String regex = regexTextField.getText();

        try {
            //noinspection ResultOfMethodCallIgnored
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            mc.thePlayer.playSound(MRSoundEvents.error, 1.0f, 1.0f);
            errorMsg = I18n.format("guiText.label.regexError");
            errorTimer = 60;
            return;
        }

        ext.setString("String", regex);
        if (routerPos != null) {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.ADD_STRING, routerPos, moduleSlotIndex, filterSlotIndex, ext));
        } else {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.ADD_STRING, hand, filterSlotIndex, ext));
        }

        regexTextField.setText("");
        getTextFieldManager().focus(0);
        errorMsg = "";
    }

    @Override
    public void resync(ItemStack stack) {
        regexList.clear();
        regexList.addAll(RegexFilter.getRegexList(stack));
        initGui();
    }

    private static class RegexTextField extends TextFieldWidget {
        private final GuiRegexFilter parent;

        RegexTextField(GuiRegexFilter parent, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
            super(parent.getTextFieldManager(), componentId, fontrendererObj, x, y, par5Width, par6Height);
            this.parent = parent;
            setMaxStringLength(40);
        }

        @Override
        public boolean textboxKeyTyped(char typedChar, int keyCode) {
            if (keyCode == Keyboard.KEY_RETURN) {
                parent.addRegex();
                return true;
            } else {
                return super.textboxKeyTyped(typedChar, keyCode);
            }
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height) {
                if (mouseButton == 1) {
                    setText("");  // right click clears field
                }
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
