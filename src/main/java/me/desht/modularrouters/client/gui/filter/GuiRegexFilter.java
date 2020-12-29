package me.desht.modularrouters.client.gui.filter;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldWidgetMR;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.smartfilter.RegexFilter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GuiRegexFilter extends GuiFilterScreen {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/regexfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 186;

    private int xPos, yPos;
    private RegexTextField regexTextField;
    private String errorMsg = "";
    private int errorTimer = 60;  // 3 seconds

    private final List<String> regexList = Lists.newArrayList();
    private final List<Buttons.DeleteButton> deleteButtons = Lists.newArrayList();

    public GuiRegexFilter(ItemStack filterStack, MFLocator locator) {
        super(filterStack, locator);

        regexList.addAll(RegexFilter.getRegexList(filterStack));
    }

    @Override
    public void init() {
        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        TextFieldManager manager = getOrCreateTextFieldManager().clear();
        regexTextField = new RegexTextField(this, 1, font, xPos + 10, yPos + 27, 144, 18);
        regexTextField.useGuiTextBackground();

        manager.focus(0);

        if (locator.filterSlot >= 0) {
            addButton(new BackButton(xPos - 12, yPos, p -> closeGUI()));
        }

        addButton(new Buttons.AddButton(xPos + 155, yPos + 23, button -> {
            if (!regexTextField.getText().isEmpty()) addRegex();
        }));

        deleteButtons.clear();
        for (int i = 0; i < RegexFilter.MAX_SIZE; i++) {
            Buttons.DeleteButton b = new Buttons.DeleteButton(xPos + 8, yPos + 52 + i * 19, i,
                    button -> sendRemovePosMessage(((Buttons.DeleteButton) button).getId()));
            addButton(b);
            deleteButtons.add(b);
        }

        updateDeleteButtonVisibility();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(textureLocation);
        blit(matrixStack, xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        font.func_243248_b(matrixStack, title, xPos + GUI_WIDTH / 2f - font.getStringPropertyWidth(title) / 2f, yPos + 6, 0x404040);

        for (int i = 0; i < regexList.size(); i++) {
            String regex = regexList.get(i);
            font.drawString(matrixStack, "/" + regex + "/", xPos + 28, yPos + 55 + i * 19, 0x404080);
        }

        if (!errorMsg.isEmpty()) {
            font.drawString(matrixStack, errorMsg, xPos + 8, yPos + 170, 0x804040);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        if (errorTimer > 0) {
            if (--errorTimer == 0) {
                errorMsg = "";
            }
        }
        super.tick();
    }

    private void addRegex() {
        try {
            String regex = regexTextField.getText();
            Pattern.compile(regex);
            sendAddStringMessage("String", regex);
            regexTextField.setText("");
            getOrCreateTextFieldManager().focus(0);
            errorMsg = "";
        } catch (PatternSyntaxException e) {
            minecraft.player.playSound(ModSounds.ERROR.get(), 1.0f, 1.0f);
            errorMsg = I18n.format("modularrouters.guiText.label.regexError");
            errorTimer = 60;
        }
    }

    @Override
    public void resync(ItemStack stack) {
        regexList.clear();
        regexList.addAll(RegexFilter.getRegexList(stack));
        updateDeleteButtonVisibility();
    }

    private void updateDeleteButtonVisibility() {
        for (int i = 0; i < deleteButtons.size(); i++) {
            deleteButtons.get(i).visible = i < regexList.size();
        }
    }

    private static class RegexTextField extends TextFieldWidgetMR {
        private final GuiRegexFilter parent;

        RegexTextField(GuiRegexFilter parent, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
            super(parent.getOrCreateTextFieldManager(), fontrendererObj, x, y, par5Width, par6Height);
            this.parent = parent;
            setMaxStringLength(40);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                parent.addRegex();
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            if (mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height) {
                if (mouseButton == 1) {
                    setText("");  // right click clears field
                }
            }
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
