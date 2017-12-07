package me.desht.modularrouters.client.gui.filter;

import com.google.common.base.Joiner;
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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiInspectionFilter extends GuiFilterScreen {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/inspectionfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 191;

    private static final int BACK_BUTTON_ID = 1;
    private static final int SUBJECT_BUTTON_ID = 2;
    private static final int OP_BUTTON_ID = 3;
    private static final int ADD_BUTTON_ID = 4;
    private static final int MATCH_BUTTON_ID = 5;
    private static final int BASE_REMOVE_ID = 100;

    private int xPos, yPos;
    private ComparisonList comparisonList;
    private IntegerTextField valueTextField;
    private InspectionSubject currentSubject = InspectionSubject.NONE;
    private InspectionOp currentOp = InspectionOp.NONE;

    public GuiInspectionFilter(ItemStack filterStack, BlockPos routerPos, Integer moduleSlotIndex, Integer filterSlotIndex, EnumHand hand) {
        super(filterStack, routerPos, moduleSlotIndex, filterSlotIndex, hand);

        comparisonList = InspectionFilter.getComparisonList(filterStack);
    }

    @Override
    public void initGui() {
        super.initGui();

        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        buttonList.clear();

        if (filterSlotIndex >= 0) {
            buttonList.add(new BackButton(BACK_BUTTON_ID, xPos - 12, yPos));
        }

        buttonList.add(new GuiButton(SUBJECT_BUTTON_ID, xPos + 8, yPos + 23, 90, 20, I18n.format("guiText.label.inspectionSubject." + currentSubject)));
        buttonList.add(new GuiButton(OP_BUTTON_ID, xPos + 95, yPos + 23, 20, 20, I18n.format("guiText.label.inspectionOp." + currentOp)));
        buttonList.add(new Buttons.AddButton(ADD_BUTTON_ID, xPos + 152, yPos + 23));

        buttonList.add(new GuiButton(MATCH_BUTTON_ID, xPos + 8, yPos + 167, 60, 20, I18n.format("guiText.label.matchAll." + comparisonList.isMatchAll())));

        for (int i = 0; i < comparisonList.items.size(); i++) {
            buttonList.add(new Buttons.DeleteButton(BASE_REMOVE_ID + i, xPos + 8, yPos + 52 + i * 19));
        }

        TextFieldManager manager = getTextFieldManager().clear();
        valueTextField = new IntegerTextField(manager, 1, fontRenderer, xPos + 120, yPos + 28, 20, 14, 0, 100);
        valueTextField.useGuiTextBackground();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        fontRenderer.drawString(title, xPos + GUI_WIDTH / 2 - this.fontRenderer.getStringWidth(title) / 2, yPos + 6, 0x404040);

        for (int i = 0; i < comparisonList.items.size(); i++) {
            InspectionMatcher.Comparison comparison = comparisonList.items.get(i);
            fontRenderer.drawString(comparison.asLocalizedText(), xPos + 28, yPos + 55 + i * 19, 0x404080);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case SUBJECT_BUTTON_ID:
                currentSubject = currentSubject.cycle(GuiScreen.isShiftKeyDown() ? -1 : 1);
                button.displayString = I18n.format("guiText.label.inspectionSubject." + currentSubject);
                break;
            case OP_BUTTON_ID:
                currentOp = currentOp.cycle(GuiScreen.isShiftKeyDown() ? -1 : 1);
                button.displayString = I18n.format("guiText.label.inspectionOp." + currentOp);
                break;
            case ADD_BUTTON_ID:
                int val = valueTextField.getValue();
                String s = Joiner.on(" ").join(currentSubject, currentOp, val);
                sendAddStringMessage("Comparison", s);
                valueTextField.setText("");
                break;
            case MATCH_BUTTON_ID:
                NBTTagCompound ext = new NBTTagCompound();
                ext.setBoolean("MatchAll", !comparisonList.isMatchAll());
                if (routerPos != null) {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.ANY_ALL_FLAG, routerPos, moduleSlotIndex, filterSlotIndex, ext));
                } else {
                    ModularRouters.network.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.ANY_ALL_FLAG, hand, filterSlotIndex, ext));
                }
            default:
                if (button.id >= BASE_REMOVE_ID && button.id < BASE_REMOVE_ID + comparisonList.items.size()) {
                    sendRemovePosMessage(button.id - BASE_REMOVE_ID);
                } else {
                    super.actionPerformed(button);
                }
        }
    }

    @Override
    public void resync(ItemStack stack) {
        comparisonList = InspectionFilter.getComparisonList(stack);
        initGui();
    }
}
