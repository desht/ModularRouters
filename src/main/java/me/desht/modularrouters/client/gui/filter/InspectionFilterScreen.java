package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.filter.Buttons.DeleteButton;
import me.desht.modularrouters.client.gui.widgets.button.BackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.item.smartfilter.InspectionFilter;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.ComparisonList;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.InspectionOp;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.InspectionSubject;
import me.desht.modularrouters.network.messages.FilterUpdateMessage;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.apache.commons.lang3.Range;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class InspectionFilterScreen extends AbstractFilterScreen {
    private static final Identifier TEXTURE_LOCATION = MiscUtil.RL("textures/gui/inspectionfilter.png");

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 191;

    private int xPos, yPos;
    private ComparisonList comparisonList;
    private IntegerTextField valueTextField;
    private InspectionSubject currentSubject = InspectionSubject.NONE;
    private InspectionOp currentOp = InspectionOp.NONE;
    private final List<DeleteButton> deleteButtons = new ArrayList<>();
    private Button matchButton;

    public InspectionFilterScreen(ItemStack filterStack, MFLocator locator) {
        super(filterStack, locator);

        comparisonList = InspectionFilter.getComparisonList(filterStack);
    }

    @Override
    public void init() {
        super.init();

        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        if (locator.filterSlot() >= 0) {
            addRenderableWidget(new BackButton(xPos - 12, yPos, _ -> closeGUI()));
        }

        addRenderableWidget(new ExtendedButton(xPos + 8, yPos + 22, 90, 20, xlate(currentSubject.getTranslationKey()), button -> {
            currentSubject = currentSubject.cycle(Minecraft.getInstance().hasShiftDown() ? -1 : 1);
            button.setMessage(xlate(currentSubject.getTranslationKey()));
        }));

        addRenderableWidget(new ExtendedButton(xPos + 95, yPos + 22, 20, 20, xlate(currentOp.getTranslationKey()), button -> {
            currentOp = currentOp.cycle(Minecraft.getInstance().hasShiftDown() ? -1 : 1);
            button.setMessage(xlate(currentOp.getTranslationKey()));
        }));

        addRenderableWidget(new Buttons.AddButton(xPos + 152, yPos + 23, _ -> addEntry()));

        matchButton = new ExtendedButton(xPos + 8, yPos + 167, 60, 20, xlate("modularrouters.guiText.label.matchAll." + comparisonList.matchAll()), _ -> {
            ItemStack newStack = Util.make(filterStack.copy(), s -> InspectionFilter.setComparisonList(s, comparisonList.setMatchAll(!comparisonList.matchAll())));
            ClientPacketDistributor.sendToServer(new FilterUpdateMessage(locator, newStack));
        });
        addRenderableWidget(matchButton);

        deleteButtons.clear();
        for (int i = 0; i < InspectionFilter.MAX_SIZE; i++) {
            DeleteButton b = new DeleteButton(xPos + 8, yPos + 52 + i * 19, i, button -> removeEntry(button.getId()));
            addRenderableWidget(b);
            deleteButtons.add(b);
        }
        updateDeleteButtonVisibility();

        valueTextField = new IntegerTextField(font, xPos + 120, yPos + 28, 20, 14, Range.of(0, 100)) {
            @Override
            public boolean keyPressed(KeyEvent event) {
                if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                    addEntry();
                    return true;
                }
                return super.keyPressed(event);
            }
        };
        valueTextField.useGuiTextBackground();

        addRenderableWidget(valueTextField);
    }

    private void updateDeleteButtonVisibility() {
        for (int i = 0; i < deleteButtons.size(); i++) {
            deleteButtons.get(i).visible = i < comparisonList.items().size();
        }
    }

    private void addEntry() {
        if (currentOp != InspectionOp.NONE && currentSubject != InspectionSubject.NONE) {
            InspectionMatcher.Comparison newEntry = new InspectionMatcher.Comparison(currentSubject, currentOp, valueTextField.getIntValue());
            ItemStack newStack = Util.make(filterStack.copy(), s -> InspectionFilter.setComparisonList(s, comparisonList.addComparison(newEntry)));
            ClientPacketDistributor.sendToServer(new FilterUpdateMessage(locator, newStack));
            valueTextField.setValue("");
        }
    }

    private void removeEntry(int pos) {
        ItemStack newStack = Util.make(filterStack.copy(), s -> InspectionFilter.setComparisonList(s, comparisonList.removeAt(pos)));
        ClientPacketDistributor.sendToServer(new FilterUpdateMessage(locator, newStack));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        graphics.text(font, title, xPos + GUI_WIDTH / 2 - font.width(title) / 2, yPos + 6, 0xFF404040, false);

        for (int i = 0; i < comparisonList.items().size(); i++) {
            InspectionMatcher.Comparison comparison = comparisonList.items().get(i);
            graphics.text(font, comparison.asLocalizedText(), xPos + 28, yPos + 55 + i * 19, 0xFF404080, false);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(graphics, pMouseX, pMouseY, pPartialTick);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_LOCATION, xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
    }

    @Override
    public void resync(ItemStack stack) {
        comparisonList = InspectionFilter.getComparisonList(stack);
        matchButton.setMessage(xlate("modularrouters.guiText.label.matchAll." + comparisonList.matchAll()));
        updateDeleteButtonVisibility();
    }
}
