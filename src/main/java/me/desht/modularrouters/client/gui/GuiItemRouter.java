package me.desht.modularrouters.client.gui;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.RouterSettingsMessage;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.List;

public class GuiItemRouter extends GuiContainerBase {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/router.png");
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
        super(new ContainerItemRouter(inventoryPlayer, router));
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        this.router = router;
    }

    @Override
    public void initGui() {
        super.initGui();
//        buttonList.clear();
        addButton(new RedstoneBehaviourButton(REDSTONE_BUTTON_ID,
                this.guiLeft + 152, this.guiTop + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getRedstoneBehaviour()) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                cycle(!isShiftKeyDown());
                router.setRedstoneBehaviour(getState());
                PacketHandler.NETWORK.sendToServer(new RouterSettingsMessage(router));
            }
        });
        addButton(new RouterEcoButton(ECO_BUTTON_ID,
                this.guiLeft + 132, this.guiTop + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getEcoMode()) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                toggle();
                router.setEcoMode(isToggled());
                PacketHandler.NETWORK.sendToServer(new RouterSettingsMessage(router));
            }
        });
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = router.getDisplayName().getString();
        fontRenderer.drawString(title, this.xSize / 2f - fontRenderer.getStringWidth(title) / 2f, LABEL_YPOS, Color.darkGray.getRGB());
        fontRenderer.drawString(I18n.format("guiText.label.buffer"), 8, BUFFER_LABEL_YPOS, Color.darkGray.getRGB());
        fontRenderer.drawString(I18n.format("guiText.label.upgrades"), ContainerItemRouter.UPGRADE_XPOS, UPGRADES_LABEL_YPOS, Color.darkGray.getRGB());
        fontRenderer.drawString(I18n.format("guiText.label.modules"), ContainerItemRouter.MODULE_XPOS, MODULE_LABEL_YPOS, Color.darkGray.getRGB());
        fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, Color.darkGray.getRGB());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        mc.getTextureManager().bindTexture(textureLocation);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    private static final int MODULE_START = ContainerItemRouter.TE_FIRST_SLOT + ContainerItemRouter.MODULE_SLOT_START;
    private static final int MODULE_END = ContainerItemRouter.TE_FIRST_SLOT + ContainerItemRouter.MODULE_SLOT_END;

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
         return Keybindings.keybindConfigure.isKeyDown() ? handleModuleConfig() : super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int btn) {
        return btn == 2 ? handleModuleConfig() : super.mouseClicked(x, y, btn);
    }

    private boolean handleModuleConfig() {
        Slot slot = getSlotUnderMouse();
        if (slot == null || !(slot.getStack().getItem() instanceof ItemModule) || slot.slotNumber < MODULE_START || slot.slotNumber > MODULE_END) {
            return false;
        }
        SlotTracker.getInstance(mc.player).setModuleSlot(slot.slotNumber - MODULE_START);
        PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(router.getPos(), slot.slotNumber - MODULE_START));
        return true;
    }

    private static class RouterEcoButton extends TexturedToggleButton {
        RouterEcoButton(int buttonId, int x, int y, int width, int height, boolean initialVal) {
            super(buttonId, x, y, width, height, initialVal);
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
            return MiscUtil.wrapString(I18n.format("guiText.tooltip.eco." + isToggled(),
                    ConfigHandler.ROUTER.ecoTimeout.get() / 20.f, ConfigHandler.ROUTER.lowPowerTickRate.get() / 20.f));
        }
    }
}
