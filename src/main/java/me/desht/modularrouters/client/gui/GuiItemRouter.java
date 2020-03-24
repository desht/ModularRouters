package me.desht.modularrouters.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.client.gui.widgets.button.RedstoneBehaviourButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.RouterSettingsMessage;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiItemRouter extends GuiContainerBase<ContainerItemRouter> implements ISendToServer, IHasContainer<ContainerItemRouter> {
    private static final ResourceLocation textureLocation = new ResourceLocation(ModularRouters.MODID, "textures/gui/router.png");
    private static final int LABEL_YPOS = 5;
    private static final int MODULE_LABEL_YPOS = 60;
    private static final int BUFFER_LABEL_YPOS = 28;
    private static final int UPGRADES_LABEL_YPOS = 28;
    private static final int GUI_HEIGHT = 186;
    private static final int GUI_WIDTH = 176;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 16;

    private static final int MODULE_START = ContainerItemRouter.TE_FIRST_SLOT + ContainerItemRouter.MODULE_SLOT_START;
    private static final int MODULE_END = ContainerItemRouter.TE_FIRST_SLOT + ContainerItemRouter.MODULE_SLOT_END;

    public final TileEntityItemRouter router;

    private RedstoneBehaviourButton rbb;
    private RouterEcoButton reb;

    public GuiItemRouter(ContainerItemRouter container, PlayerInventory inventoryPlayer, ITextComponent displayName) {
        super(container, inventoryPlayer, displayName);

        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        this.router = container.getRouter();
    }

    @Override
    public void init() {
        super.init();

        addButton(rbb = new RedstoneBehaviourButton(this.guiLeft + 152, this.guiTop + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getRedstoneBehaviour(), this));
        addButton(reb = new RouterEcoButton(this.guiLeft + 132, this.guiTop + 10, BUTTON_WIDTH, BUTTON_HEIGHT, router.getEcoMode()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("block.modularrouters.item_router");
        font.drawString(title, this.xSize / 2f - font.getStringWidth(title) / 2f, LABEL_YPOS, 0xFF404040);
        font.drawString(I18n.format("guiText.label.buffer"), 8, BUFFER_LABEL_YPOS, 0xFF404040);
        font.drawString(I18n.format("guiText.label.upgrades"), ContainerItemRouter.UPGRADE_XPOS, UPGRADES_LABEL_YPOS, 0xFF404040);
        font.drawString(I18n.format("guiText.label.modules"), ContainerItemRouter.MODULE_XPOS, MODULE_LABEL_YPOS, 0xFF404040);
        font.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 4, 0xFF404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        minecraft.getTextureManager().bindTexture(textureLocation);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
         return Keybindings.keybindConfigure.getKey().getKeyCode() == keyCode ? handleModuleConfig() : super.keyPressed(keyCode, scanCode, modifiers);
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
        MFLocator locator = MFLocator.moduleInRouter(router.getPos(), slot.slotNumber - MODULE_START);
        PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
        return true;
    }

    @Override
    public void sendToServer() {
        PacketHandler.NETWORK.sendToServer(new RouterSettingsMessage(router, rbb.getState(), reb.isToggled()));
    }

    private class RouterEcoButton extends TexturedToggleButton {
        RouterEcoButton(int x, int y, int width, int height, boolean initialVal) {
            super(x, y, width, height, initialVal, GuiItemRouter.this);
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
                    MRConfig.Common.Router.ecoTimeout / 20.f, MRConfig.Common.Router.lowPowerTickRate / 20.f));
        }
    }
}
