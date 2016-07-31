package me.desht.modularrouters.gui;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ItemRouterContainer;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.network.RouterSettingsMessage;
import me.desht.modularrouters.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiItemRouter extends GuiContainerBase {
    public static final int LABEL_XPOS = 5;
    public static final int LABEL_YPOS = 5;
    public static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.modId, "textures/gui/router.png");
    public static final int MODULE_LABEL_YPOS = 60;
    public static final int BUFFER_LABEL_YPOS = 28;
    public static final int UPGRADES_LABEL_YPOS = 28;
    public static final int GUI_HEIGHT = 185;
    public static final int GUI_WIDTH = 176;
    public static final int BUTTON_HEIGHT = 16;
    public static final int BUTTON_WIDTH = 16;
    private static final int REDSTONE_BUTTON_ID = 1;

    public final TileEntityItemRouter tileEntityItemRouter;

    public GuiItemRouter(InventoryPlayer inventoryPlayer, TileEntityItemRouter tileEntityItemRouter) {
        super(new ItemRouterContainer(inventoryPlayer, tileEntityItemRouter));
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        this.tileEntityItemRouter = tileEntityItemRouter;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.buttonList.add(new RouterRedstoneButton(REDSTONE_BUTTON_ID,
                this.guiLeft + 152, this.guiTop + 10, BUTTON_WIDTH, BUTTON_HEIGHT, textureLocation, tileEntityItemRouter.getRedstoneBehaviour()));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case REDSTONE_BUTTON_ID:
                RouterRedstoneButton rrb = (RouterRedstoneButton) button;
                rrb.cycle(!isShiftKeyDown());
                CommonProxy.network.sendToServer(new RouterSettingsMessage(tileEntityItemRouter, rrb.getState()));
                tileEntityItemRouter.setRedstoneBehaviour(rrb.getState());
                break;
            default:
                break;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = tileEntityItemRouter.getDisplayName().getUnformattedText();
        fontRendererObj.drawString(title, this.xSize / 2 - this.fontRendererObj.getStringWidth(title) / 2, LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("guiText.label.buffer"), 8, BUFFER_LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("guiText.label.upgrades"), 98, UPGRADES_LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("guiText.label.modules"), 8, MODULE_LABEL_YPOS, Color.darkGray.getRGB());
        fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, Color.darkGray.getRGB());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
