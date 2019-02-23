package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.glfw.GLFW;

abstract class GuiFilterContainer extends GuiContainerBase {
    protected final TileEntityItemRouter router;
//    protected final Integer moduleSlotIndex;  // slot of the module in the router
//    protected final Integer filterSlotIndex;  // slot of the filter item in the module
    protected final EnumHand hand;
    protected final String title;
    protected final ItemStack filterStack;

    GuiFilterContainer(ContainerSmartFilter container) {
        super(container);
        this.router = container.getRouter();
        this.hand = container.getHand();
        this.filterStack = container.getFilterStack();
        this.title = filterStack.getDisplayName().getString();
    }

    boolean closeGUI() {
        SlotTracker.getInstance(mc.player).clearFilterSlot();
        // need to re-open module GUI for module in router slot <moduleSlotIndex>
        if (router != null) {
//           router.playerConfiguringModule(mc.player, moduleSlotIndex);
            PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(router.getPos(), SlotTracker.getInstance(mc.player).getModuleSlot()));
            return true;
        } else if (hand != null) {
            ItemStack stack = mc.player.getHeldItem(hand);
            if (stack.getItem() instanceof ItemModule) {
                // need to re-open module GUI for module in player's hand
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInHand(hand));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E)) {
            // Intercept ESC/E and immediately reopen the previous GUI, if any
            return closeGUI();
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
