package me.desht.modularrouters.client.gui.filter;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.BackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.item.smartfilter.InspectionFilter;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.ComparisonList;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.InspectionOp;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.InspectionSubject;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.FilterSettingsMessage.Operation;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GuiInspectionFilter extends GuiFilterScreen {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/inspectionfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 191;

    private int xPos, yPos;
    private ComparisonList comparisonList;
    private IntegerTextField valueTextField;
    private InspectionSubject currentSubject = InspectionSubject.NONE;
    private InspectionOp currentOp = InspectionOp.NONE;
    private List<Buttons.DeleteButton> deleteButtons = new ArrayList<>();
    private Button matchButton;

    public GuiInspectionFilter(ItemStack filterStack, MFLocator locator) {
        super(filterStack, locator);

        comparisonList = InspectionFilter.getComparisonList(filterStack);
    }

    @Override
    public void init() {
        super.init();

        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        if (locator.filterSlot >= 0) {
            addButton(new BackButton(xPos - 12, yPos, button -> closeGUI()));
        }

        addButton(new Button(xPos + 8, yPos + 22, 90, 20, I18n.format("guiText.label.inspectionSubject." + currentSubject), button -> {
            currentSubject = currentSubject.cycle(Screen.hasShiftDown() ? -1 : 1);
            button.setMessage(I18n.format("guiText.label.inspectionSubject." + currentSubject));
        }));

        addButton(new Button(xPos + 95, yPos + 22, 20, 20, I18n.format("guiText.label.inspectionOp." + currentOp), button -> {
            currentOp = currentOp.cycle(Screen.hasShiftDown() ? -1 : 1);
            button.setMessage(I18n.format("guiText.label.inspectionOp." + currentOp));
        }));

        addButton(new Buttons.AddButton(xPos + 152, yPos + 23, button -> addEntry()));

        matchButton = new Button(xPos + 8, yPos + 167, 60, 20, I18n.format("guiText.label.matchAll." + comparisonList.isMatchAll()), button -> {
            CompoundNBT ext = new CompoundNBT();
            ext.putBoolean("MatchAll", !comparisonList.isMatchAll());
            PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.ANY_ALL_FLAG, locator, ext));
        });
        addButton(matchButton);

        for (int i = 0; i < InspectionFilter.MAX_SIZE; i++) {
            Buttons.DeleteButton b = new Buttons.DeleteButton(xPos + 8, yPos + 52 + i * 19, i, button -> sendRemovePosMessage(((Buttons.DeleteButton) button).getId()));
            addButton(b);
            deleteButtons.add(b);
        }
        updateDeleteButtonVisibility();

        TextFieldManager manager = getTextFieldManager().clear();
        valueTextField = new IntegerTextField(manager, font, xPos + 120, yPos + 28, 20, 14, 0, 100) {
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    addEntry();
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
        valueTextField.useGuiTextBackground();
    }

    private void updateDeleteButtonVisibility() {
        for (int i = 0; i < deleteButtons.size(); i++) {
            deleteButtons.get(i).visible = i < comparisonList.items.size();
        }
    }

    private void addEntry() {
        int val = valueTextField.getValue();
        String s = Joiner.on(" ").join(currentSubject, currentOp, val);
        sendAddStringMessage("Comparison", s);
        valueTextField.setText("");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(textureLocation);
        blit(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        font.drawString(title, xPos + GUI_WIDTH / 2f - this.font.getStringWidth(title) / 2f, yPos + 6, 0x404040);

        for (int i = 0; i < comparisonList.items.size(); i++) {
            InspectionMatcher.Comparison comparison = comparisonList.items.get(i);
            font.drawString(comparison.asLocalizedText(), xPos + 28, yPos + 55 + i * 19, 0x404080);
        }

        super.render(mouseX, mouseY, partialTicks);

    }

    @Override
    public void resync(ItemStack stack) {
        comparisonList = InspectionFilter.getComparisonList(stack);
        matchButton.setMessage(I18n.format("guiText.label.matchAll." + comparisonList.isMatchAll()));
        updateDeleteButtonVisibility();
    }
}
