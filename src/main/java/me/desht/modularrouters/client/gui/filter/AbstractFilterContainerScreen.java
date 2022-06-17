package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.AbstractMRContainerScreen;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractFilterContainerScreen extends AbstractMRContainerScreen<ContainerSmartFilter> {
    protected final InteractionHand hand;
    protected final String title;
    protected final ItemStack filterStack;

    AbstractFilterContainerScreen(ContainerSmartFilter container, Inventory inv, Component displayName) {
        super(container, inv, displayName);

        this.hand = container.getLocator().hand;
        this.filterStack = container.getFilterStack();
        this.title = filterStack.getHoverName().getString();
    }

    boolean closeGUI() {
        // need to re-open module GUI for module in router slot <moduleSlotIndex>
        MFLocator locator = menu.getLocator();
        if (locator.routerPos != null) {
            PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
            return true;
        } else if (hand != null) {
            ItemStack stack = getMinecraft().player.getItemInHand(hand);
            if (stack.getItem() instanceof ModuleItem) {
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
