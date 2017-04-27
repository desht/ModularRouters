package me.desht.modularrouters.gui.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.widgets.GuiScreenBase;
import me.desht.modularrouters.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import me.desht.modularrouters.network.SyncUpgradeSettingsMessage;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiSyncUpgrade extends GuiScreenBase implements GuiPageButtonList.GuiResponder {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/sync_upgrade.png");
    private static final ItemStack clockStack = new ItemStack(Items.CLOCK);
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 48;
    private static final int VALUE_TEXTFIELD_ID = 1;
    private static final int TOOLTIP_BUTTON_ID = 1;

    private final String title;

    private int xPos, yPos;
    private int tunedValue;

    public GuiSyncUpgrade(ItemStack upgradeStack) {
        this.title = upgradeStack.getDisplayName();
        this.tunedValue = SyncUpgrade.getTunedValue(upgradeStack);
    }

    @Override
    public void initGui() {
        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        TextFieldManager manager = getTextFieldManager().clear();
        IntegerTextField intField = new IntegerTextField(manager, VALUE_TEXTFIELD_ID, fontRenderer, xPos + 77, yPos + 27, 25, 16, 0, Config.baseTickRate - 1);
        intField.setValue(tunedValue);
        intField.setGuiResponder(this);
        intField.useGuiTextBackground();

        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_ID, xPos + 55, yPos + 24, 16, 16, clockStack));

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        fontRenderer.drawString(title, xPos + GUI_WIDTH / 2 - fontRenderer.getStringWidth(title) / 2, yPos + 6, 0x404040);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }

    @Override
    public void setEntryValue(int id, boolean value) {

    }

    @Override
    public void setEntryValue(int id, float value) {

    }

    @Override
    public void setEntryValue(int id, String value) {
        if (id == VALUE_TEXTFIELD_ID) {
            tunedValue = value.isEmpty() ? 0 : Integer.parseInt(value);
            sendSettingsDelayed(5);
        }
    }

    @Override
    protected void sendSettingsToServer() {
        ModularRouters.network.sendToServer(new SyncUpgradeSettingsMessage(tunedValue));
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int buttonId, int x, int y, int width, int height, ItemStack renderStack) {
            super(buttonId, x, y, width, height, renderStack, true);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.tunedValue", 0, Config.baseTickRate - 1);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }
}
