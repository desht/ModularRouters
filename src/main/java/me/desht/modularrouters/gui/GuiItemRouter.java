package me.desht.modularrouters.gui;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ItemRouterContainer;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.gui.widgets.TexturedToggleButton;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.RouterSettingsMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuiItemRouter extends GuiContainerBase {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/router.png");
    public static final int LABEL_XPOS = 5;
    public static final int LABEL_YPOS = 5;
    public static final int MODULE_LABEL_YPOS = 60;
    public static final int BUFFER_LABEL_YPOS = 28;
    public static final int UPGRADES_LABEL_YPOS = 28;
    public static final int GUI_HEIGHT = 186;
    public static final int GUI_WIDTH = 176;
    public static final int BUTTON_HEIGHT = 16;
    public static final int BUTTON_WIDTH = 16;
    private static final int REDSTONE_BUTTON_ID = 1;
    private static final int ECO_BUTTON_ID = 2;

    public final TileEntityItemRouter router;

    public GuiItemRouter(InventoryPlayer inventoryPlayer, TileEntityItemRouter router) {
        super(new ItemRouterContainer(inventoryPlayer, router));
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        this.router = router;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(new RedstoneBehaviourButton(REDSTONE_BUTTON_ID,
                this.guiLeft + 152, this.guiTop + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getRedstoneBehaviour()));
        buttonList.add(new RouterEcoButton(ECO_BUTTON_ID,
                this.guiLeft + 132, this.guiTop + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getEcoMode()));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case REDSTONE_BUTTON_ID:
                RedstoneBehaviourButton rrb = (RedstoneBehaviourButton) button;
                rrb.cycle(!isShiftKeyDown());
                router.setRedstoneBehaviour(rrb.getState());
                ModularRouters.network.sendToServer(new RouterSettingsMessage(router));
                break;
            case ECO_BUTTON_ID:
                RouterEcoButton reb = (RouterEcoButton) button;
                reb.toggle();
                router.setEcoMode(reb.isToggled());
                ModularRouters.network.sendToServer(new RouterSettingsMessage(router));
            default:
                break;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = router.getDisplayName().getUnformattedText();
        fontRendererObj.drawString(title, this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2, LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("guiText.label.buffer"), 8, BUFFER_LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("guiText.label.upgrades"), 98, UPGRADES_LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("guiText.label.modules"), 8, MODULE_LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, Color.darkGray.getRGB());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        mc.getTextureManager().bindTexture(textureLocation);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    private static final int MODULE_START = ItemRouterContainer.TE_FIRST_SLOT + ItemRouterContainer.MODULE_SLOT_START;
    private static final int MODULE_END = ItemRouterContainer.TE_FIRST_SLOT + ItemRouterContainer.MODULE_SLOT_END;

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (typedChar == Config.configKey) {
            Slot slot = getSlotUnderMouse();
            if (slot != null && slot.slotNumber >= MODULE_START && slot.slotNumber < MODULE_END
                    && slot.getHasStack() && slot.getStack().getItem() instanceof ItemModule) {
                ModularRouters.network.sendToServer(OpenGuiMessage.openModuleInRouter(router.getPos(), slot.getSlotIndex()));
                router.playerConfiguringModule(mc.thePlayer, slot.getSlotIndex());
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    private static class RouterEcoButton extends TexturedToggleButton {
        RouterEcoButton(int buttonId, int x, int y, int width, int height, boolean initialVal) {
            super(buttonId, x, y, width, height);
            setToggled(initialVal);
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 96 : 80;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }

        @Override
        public List<String> getTooltip() {
            String s = I18n.format("guiText.tooltip.eco." + isToggled(), Config.ecoTimeout / 20.f, Config.lowPowerTickRate / 20.f);
            return Arrays.asList(s.split("\\\\n"));
        }
    }
}
