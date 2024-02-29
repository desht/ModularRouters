package me.desht.modularrouters.client.gui.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackButton;
import me.desht.modularrouters.client.gui.widgets.textfield.IntegerTextField;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import me.desht.modularrouters.network.messages.SyncUpgradeSettingsMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.Range;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class SyncUpgradeScreen extends Screen {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(ModularRouters.MODID, "textures/gui/sync_upgrade.png");
    private static final ItemStack clockStack = new ItemStack(Items.CLOCK);
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 48;

    private final String title;

    private int xPos;
    private int yPos;
    private final int currentVal;
    private final InteractionHand hand;

    private IntegerTextField intField;

    public SyncUpgradeScreen(ItemStack upgradeStack, InteractionHand hand) {
        super(upgradeStack.getHoverName());

        title = upgradeStack.getHoverName().getString();
        currentVal = SyncUpgrade.getTunedValue(upgradeStack);
        this.hand = hand;
    }

    public static void openSyncGui(ItemStack stack, InteractionHand hand) {
        Minecraft.getInstance().setScreen(new SyncUpgradeScreen(stack, hand));
    }

    @Override
    public void init() {
        super.init();

        xPos = (width - GUI_WIDTH) / 2;
        yPos = (height - GUI_HEIGHT) / 2;

        intField = new IntegerTextField(font,
                xPos + 77, yPos + 27, 25, 16, Range.of(0, ConfigHolder.common.router.baseTickRate.get() - 1));
        intField.setValue(currentVal);
        intField.useGuiTextBackground();
        intField.setFocused(true);
        setFocused(intField);

        addRenderableWidget(intField);
        addRenderableWidget(new TooltipButton(xPos + 55, yPos + 24, 16, 16));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawString(font, title, xPos + GUI_WIDTH / 2 - font.width(title) / 2, yPos + 6, 0x404040, false);
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.blit(TEXTURE_LOCATION, xPos, yPos, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        int newVal = intField.getIntValue();
        if (currentVal != newVal) {
            PacketDistributor.SERVER.noArg().send(new SyncUpgradeSettingsMessage(newVal, hand));
        }

        super.onClose();
    }

    private static class TooltipButton extends ItemStackButton {
        TooltipButton(int x, int y, int width, int height) {
            super(x, y, width, height, clockStack, true, p -> {});
            ClientUtil.setMultilineTooltip(this,
                    xlate("modularrouters.guiText.tooltip.tunedValue", 0, ConfigHolder.common.router.baseTickRate.get() - 1).withStyle(ChatFormatting.AQUA),
                    xlate("modularrouters.guiText.tooltip.numberFieldTooltip")
            );
        }

        @Override
        public void playDownSound(SoundManager soundHandlerIn) {
            // no sound
        }
    }
}
