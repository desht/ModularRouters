package me.desht.modularrouters.gui.widgets;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public abstract class GuiContainerBase extends GuiContainer {
    private final List<TextFieldWidget> textFields = Lists.newArrayList();
    private int focusedField = -1;

    public GuiContainerBase(Container c) {
        super(c);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void handleTooltip(GuiScreenEvent.DrawScreenEvent.Post event) {
        // drawing tooltip here ensures it's on top of everything that's drawn, including by subclasses
        this.buttonList.stream().filter(button -> button.isMouseOver() && button instanceof ITooltipButton).forEach(button -> {
            drawHoveringText(((ITooltipButton) button).getTooltip(), event.getMouseX(), event.getMouseY(), fontRendererObj);
        });
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        textFields.forEach(TextFieldWidget::drawTextBox);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (focusedField >= 0) {
            textFields.get(focusedField).updateCursorCounter();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (focusedField >= 0 && wheel != 0) {
            textFields.get(focusedField).onMouseWheel(wheel < 0 ? -1 : 1);
        } else {
            super.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        super.mouseClicked(x, y, btn);
        for (TextFieldWidget field : textFields) {
            field.mouseClicked(x, y, btn);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (focusedField >= 0) {
            textFields.get(focusedField).textboxKeyTyped(typedChar, keyCode);
            if (keyCode == Keyboard.KEY_E) return;  // avoid closing window while text field focused
        } else if (keyCode == Keyboard.KEY_TAB) {
            if (GuiScreen.isShiftKeyDown()) {
                focusPrev();
            } else {
                focusNext();
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    public void addTextField(TextFieldWidget textField) {
        textFields.add(textField);
    }

    public void focus(int field) {
        if (field != focusedField) {
            textFields.get(field).setFocused(false);
            focusedField = field;
            textFields.get(focusedField).setFocused(true);
        }
    }

    public void focusNext() {
        int field = focusedField + 1;
        if (field > textFields.size()) field = 0;
        focus(field);
    }

    public void focusPrev() {
        int field = focusedField - 1;
        if (field < 0) field = textFields.size() - 1;
        focus(field);
    }

    protected void onTextFieldFocusChange(int id, boolean newFocus) {
        focusedField = newFocus ? id : -1;
    }
}
