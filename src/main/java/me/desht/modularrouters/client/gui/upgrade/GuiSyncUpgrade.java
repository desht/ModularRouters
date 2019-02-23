package me.desht.modularrouters.client.gui.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.widgets.GuiScreenBase;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.SyncUpgradeSettingsMessage;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiSyncUpgrade extends GuiScreenBase {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/sync_upgrade.png");
    private static final ItemStack clockStack = new ItemStack(Items.CLOCK);
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 48;
    private static final int VALUE_TEXTFIELD_ID = 1;
    private static final int TOOLTIP_BUTTON_ID = 1;

    private final String title;

    private int xPos, yPos;
    private int tunedValue;

    public GuiSyncUpgrade(ItemStack upgradeStack) {
        this.title = upgradeStack.getDisplayName().getString();
        this.tunedValue = SyncUpgrade.getTunedValue(upgradeStack);
    }

    @Override
    public void initGui() {
        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        TextFieldManager manager = getTextFieldManager().clear();
        IntegerTextField intField = new IntegerTextField(manager, VALUE_TEXTFIELD_ID, fontRenderer,
                xPos + 77, yPos + 27, 25, 16, 0, ConfigHandler.ROUTER.baseTickRate.get() - 1);
        intField.setTextAcceptHandler((id, s) -> {
            if (id == VALUE_TEXTFIELD_ID) {
                tunedValue = s.isEmpty() ? 0 : Integer.parseInt(s);
                sendSettingsDelayed(5);
            }
        });
        intField.setValue(tunedValue);
        intField.useGuiTextBackground();

        addButton(new TooltipButton(TOOLTIP_BUTTON_ID, xPos + 55, yPos + 24, 16, 16, clockStack));

        super.initGui();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(textureLocation);
        drawTexturedModalRect(xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
        fontRenderer.drawString(title, xPos + GUI_WIDTH / 2f - fontRenderer.getStringWidth(title) / 2f, yPos + 6, 0x404040);

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void sendSettingsToServer() {
        PacketHandler.NETWORK.sendToServer(new SyncUpgradeSettingsMessage(tunedValue));
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int buttonId, int x, int y, int width, int height, ItemStack renderStack) {
            super(buttonId, x, y, width, height, renderStack, true);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.tunedValue", 0, ConfigHandler.ROUTER.baseTickRate.get() - 1);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.numberFieldTooltip");
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no sound
        }
    }
}
