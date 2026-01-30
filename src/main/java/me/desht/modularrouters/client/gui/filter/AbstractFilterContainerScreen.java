package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.IResyncableGui;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.network.messages.OpenGuiMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractFilterContainerScreen extends AbstractContainerScreen<AbstractSmartFilterMenu> implements IResyncableGui {
    protected final InteractionHand hand;
    protected final String title;
    protected final ItemStack filterStack;

    AbstractFilterContainerScreen(AbstractSmartFilterMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);

        this.hand = container.getLocator().hand();
        this.filterStack = container.getFilterStack();
        this.title = filterStack.getHoverName().getString();
    }

    boolean closeGUI() {
        MFLocator locator = menu.getLocator();
        if (locator.routerPos() != null) {
            // need to re-open module GUI for module in router slot
            ClientPacketDistributor.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
            return true;
        } else if (hand != null) {
            ItemStack stack = getMinecraft().player.getItemInHand(hand);
            if (stack.getItem() instanceof ModuleItem) {
                // need to re-open module GUI for module in player's hand
                ClientPacketDistributor.sendToServer(OpenGuiMessage.openModuleInHand(locator));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if ((event.key() == GLFW.GLFW_KEY_ESCAPE || ClientUtil.isInvKey(event.key()))) {
            // Intercept ESC/E and immediately reopen the previous GUI, if any
            if (closeGUI()) return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    public void resync(ItemStack stack) {
        // nothing by default
    }
}
