package me.desht.modularrouters.client.gui.filter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.filter.Buttons.DeleteButton;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldWidgetMR;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.item.smartfilter.RegexFilter;
import me.desht.modularrouters.network.messages.FilterUpdateMessage;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class RegexFilterScreen extends AbstractFilterScreen {
    private static final ResourceLocation TEXTURE_LOCATION = MiscUtil.RL("textures/gui/regexfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 186;

    private int xPos, yPos;
    private RegexTextField regexTextField;
    private Component errorMsg = Component.empty();
    private int errorTimer = 60;  // 3 seconds

    private final List<String> regexList;
    private final List<DeleteButton> deleteButtons = Lists.newArrayList();

    public RegexFilterScreen(ItemStack filterStack, MFLocator locator) {
        super(filterStack, locator);

        regexList = RegexFilter.getRegexList(filterStack);
    }

    @Override
    public void init() {
        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        regexTextField = new RegexTextField(this, font, xPos + 10, yPos + 27, 144, 18);
        regexTextField.useGuiTextBackground();
        addRenderableWidget(regexTextField);

        if (locator.filterSlot() >= 0) {
            addRenderableWidget(new BackButton(xPos - 12, yPos, p -> closeGUI()));
        }

        addRenderableWidget(new Buttons.AddButton(xPos + 155, yPos + 23, button -> addRegex()));

        deleteButtons.clear();
        for (int i = 0; i < RegexFilter.MAX_SIZE; i++) {
            DeleteButton b = new DeleteButton(xPos + 8, yPos + 52 + i * 19, i, button -> {
                sendRegexToServer(button.removeFromList(new ArrayList<>(regexList)));
            });
            addRenderableWidget(b);
            deleteButtons.add(b);
        }

        updateDeleteButtonVisibility();
    }

    private void sendRegexToServer(Collection<String> newFilters) {
        ItemStack newStack = Util.make(filterStack.copy(), s -> ModFilter.setModList(s, List.copyOf(newFilters)));
        ClientPacketDistributor.sendToServer(new FilterUpdateMessage(locator, newStack));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawString(font, title, xPos + GUI_WIDTH / 2 - font.width(title) / 2, yPos + 6, 0x404040, false);

        for (int i = 0; i < regexList.size(); i++) {
            String regex = regexList.get(i);
            graphics.drawString(font, "/" + regex + "/", xPos + 28, yPos + 55 + i * 19, 0x404080, false);
        }

        graphics.drawString(font, errorMsg, xPos + 8, yPos + 170, 0x804040, false);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(graphics, pMouseX, pMouseY, pPartialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
    }

    @Override
    public void tick() {
        if (errorTimer > 0 && --errorTimer == 0) {
            errorMsg = Component.empty();
        }
        super.tick();
    }

    private void addRegex() {
        try {
            String regex = regexTextField.getValue();
            if (!regex.isEmpty()) {
                Pattern.compile(regex);  // just to validate syntax
                List<String> updatedList = new ArrayList<>(regexList);
                updatedList.add(regex);
                ItemStack newStack = Util.make(filterStack.copy(), s -> RegexFilter.setRegexList(s, updatedList));
                ClientPacketDistributor.sendToServer(new FilterUpdateMessage(locator, newStack));
                regexTextField.setValue("");
                errorMsg = Component.empty();
            }
        } catch (PatternSyntaxException e) {
            minecraft.player.playSound(ModSounds.ERROR.get(), 1.0f, 1.0f);
            errorMsg = xlate("modularrouters.guiText.label.regexError");
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
        private final RegexFilterScreen parent;

        RegexTextField(RegexFilterScreen parent, Font fontrendererObj, int x, int y, int par5Width, int par6Height) {
            super(fontrendererObj, x, y, par5Width, par6Height);
            this.parent = parent;
            setMaxLength(40);
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
            if (mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= this.getY() && mouseY < this.getY() + this.height) {
                if (mouseButton == 1) {
                    setValue("");  // right click clears field
                }
            }
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
