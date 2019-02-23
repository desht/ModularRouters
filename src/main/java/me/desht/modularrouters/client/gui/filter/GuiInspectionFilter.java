package me.desht.modularrouters.client.gui.filter;

import com.google.common.base.Joiner;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.BackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.item.smartfilter.InspectionFilter;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.ComparisonList;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.InspectionOp;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.InspectionSubject;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

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

    public GuiInspectionFilter(ItemStack filterStack, TileEntityItemRouter router, EnumHand hand) {
        super(filterStack, router, hand);

        comparisonList = InspectionFilter.getComparisonList(filterStack);
    }

    @Override
    public void initGui() {
        super.initGui();

        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

//        buttonList.clear();

        if (SlotTracker.getInstance(mc.player).getFilterSlot() >= 0) {
            addButton(new BackButton(BACK_BUTTON_ID, xPos - 12, yPos) {
                @Override
                public void onClick(double p_194829_1_, double p_194829_3_) {
                    closeGUI();
                }
            });
        }

        addButton(new GuiButton(SUBJECT_BUTTON_ID, xPos + 8, yPos + 23, 90, 20, I18n.format("guiText.label.inspectionSubject." + currentSubject)) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                currentSubject = currentSubject.cycle(GuiScreen.isShiftKeyDown() ? -1 : 1);
                displayString = I18n.format("guiText.label.inspectionSubject." + currentSubject);
            }
        });
        addButton(new GuiButton(OP_BUTTON_ID, xPos + 95, yPos + 23, 20, 20, I18n.format("guiText.label.inspectionOp." + currentOp)) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                currentOp = currentOp.cycle(GuiScreen.isShiftKeyDown() ? -1 : 1);
                displayString = I18n.format("guiText.label.inspectionOp." + currentOp);
            }
        });
        addButton(new Buttons.AddButton(ADD_BUTTON_ID, xPos + 152, yPos + 23) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                int val = valueTextField.getValue();
                String s = Joiner.on(" ").join(currentSubject, currentOp, val);
                sendAddStringMessage("Comparison", s);
                valueTextField.setText("");
            }
        });

        addButton(new GuiButton(MATCH_BUTTON_ID, xPos + 8, yPos + 167, 60, 20, I18n.format("guiText.label.matchAll." + comparisonList.isMatchAll())) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                NBTTagCompound ext = new NBTTagCompound();
                ext.putBoolean("MatchAll", !comparisonList.isMatchAll());
                if (router != null) {
                    PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.ANY_ALL_FLAG, router.getPos(), ext));
                } else {
                    PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                            FilterSettingsMessage.Operation.ANY_ALL_FLAG, hand, ext));
                }
            }
        });

        for (int i = 0; i < comparisonList.items.size(); i++) {
            addButton(new Buttons.DeleteButton(BASE_REMOVE_ID + i, xPos + 8, yPos + 52 + i * 19) {
                @Override
                public void onClick(double p_194829_1_, double p_194829_3_) {
                    if (id >= BASE_REMOVE_ID && id < BASE_REMOVE_ID + comparisonList.items.size()) {
                        sendRemovePosMessage(id - BASE_REMOVE_ID);
                    }
                }
            });
        }

        TextFieldManager manager = getTextFieldManager().clear();
        valueTextField = new IntegerTextField(manager, 1, fontRenderer, xPos + 120, yPos + 28, 20, 14, 0, 100);
        valueTextField.useGuiTextBackground();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        fontRenderer.drawString(title, xPos + GUI_WIDTH / 2f - this.fontRenderer.getStringWidth(title) / 2f, yPos + 6, 0x404040);

        for (int i = 0; i < comparisonList.items.size(); i++) {
            InspectionMatcher.Comparison comparison = comparisonList.items.get(i);
            fontRenderer.drawString(comparison.asLocalizedText(), xPos + 28, yPos + 55 + i * 19, 0x404080);
        }

        super.render(mouseX, mouseY, partialTicks);

    }

    @Override
    public void resync(ItemStack stack) {
        comparisonList = InspectionFilter.getComparisonList(stack);
        initGui();
    }
}
