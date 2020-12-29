package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

abstract class GuiFilterContainer extends GuiContainerBase<ContainerSmartFilter> {
    protected final Hand hand;
    protected final String title;
    protected final ItemStack filterStack;

    GuiFilterContainer(ContainerSmartFilter container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);

        this.hand = container.getLocator().hand;
        this.filterStack = container.getFilterStack();
        this.title = filterStack.getDisplayName().getString();
    }

    boolean closeGUI() {
        // need to re-open module GUI for module in router slot <moduleSlotIndex>
        MFLocator locator = container.getLocator();
        if (locator.routerPos != null) {
            PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
            return true;
        } else if (hand != null) {
            ItemStack stack = getMinecraft().player.getHeldItem(hand);
            if (stack.getItem() instanceof ItemModule) {
                // need to re-open module GUI for module in player's hand
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInHand(locator));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ESCAPE || ClientUtil.isInvKey(keyCode))) {
            // Intercept ESC/E and immediately reopen the previous GUI, if any
            if (closeGUI()) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
