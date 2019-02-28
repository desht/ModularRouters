package me.desht.modularrouters.client.gui.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.BackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldWidget;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.item.smartfilter.RegexFilter;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class GuiRegexFilter extends GuiFilterScreen {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/regexfilter.png");

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
    private final List<Buttons.DeleteButton> deleteButtons = Lists.newArrayList();

    public GuiRegexFilter(ItemStack filterStack, TileEntityItemRouter router, EnumHand hand) {
        super(filterStack, router, hand);

        regexList.addAll(RegexFilter.getRegexList(filterStack));
    }

    @Override
    public void initGui() {
        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        TextFieldManager manager = getTextFieldManager().clear();
        regexTextField = new RegexTextField(this, 1, fontRenderer, xPos + 10, yPos + 27, 144, 18);
        regexTextField.useGuiTextBackground();

        manager.focus(0);

        if (SlotTracker.getInstance(mc.player).getFilterSlot() >= 0) {
            addButton(new BackButton(BACK_BUTTON_ID, xPos - 12, yPos) {
                @Override
                public void onClick(double p_194829_1_, double p_194829_3_) {
                    closeGUI();
                }
            });
        }
        addButton(new Buttons.AddButton(ADD_REGEX_ID, xPos + 155, yPos + 23) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                if (!regexTextField.getText().isEmpty()) addRegex();
            }
        });
        for (int i = 0; i < RegexFilter.MAX_SIZE; i++) {
            Buttons.DeleteButton b = new Buttons.DeleteButton(BASE_REMOVE_ID + i, xPos + 8, yPos + 52 + i * 19) {
                @Override
                public void onClick(double p_194829_1_, double p_194829_3_) {
                    if (id >= BASE_REMOVE_ID && id < BASE_REMOVE_ID + regexList.size()) {
                        sendRemovePosMessage(id - BASE_REMOVE_ID);
                    }
                }
            };
            addButton(b);
            deleteButtons.add(b);
        }
        updateDeleteButtonVisibility();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        fontRenderer.drawString(title, xPos + GUI_WIDTH / 2f - fontRenderer.getStringWidth(title) / 2f, yPos + 6, 0x404040);

        for (int i = 0; i < regexList.size(); i++) {
            String regex = regexList.get(i);
            fontRenderer.drawString("/" + regex + "/", xPos + 28, yPos + 55 + i * 19, 0x404080);
        }

        if (!errorMsg.isEmpty()) {
            fontRenderer.drawString(errorMsg, xPos + 8, yPos + 170, 0x804040);
        }

        super.render(mouseX, mouseY, partialTicks);
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
            getTextFieldManager().focus(0);
            errorMsg = "";
        } catch (PatternSyntaxException e) {
            mc.player.playSound(ObjectRegistry.SOUND_ERROR, 1.0f, 1.0f);
            errorMsg = I18n.format("guiText.label.regexError");
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

    private static class RegexTextField extends TextFieldWidget {
        private final GuiRegexFilter parent;

        RegexTextField(GuiRegexFilter parent, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
            super(parent.getTextFieldManager(), componentId, fontrendererObj, x, y, par5Width, par6Height);
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
