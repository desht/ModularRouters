package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.IResyncableGui;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.network.messages.OpenGuiMessage;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractFilterContainerScreen extends AbstractContainerScreen<AbstractSmartFilterMenu> implements IResyncableGui {
    protected final String title;
    protected final ItemStack filterStack;

    AbstractFilterContainerScreen(AbstractSmartFilterMenu container, Inventory inv, Component displayName, int width, int height) {
        super(container, inv, displayName, width, height);

        this.filterStack = container.getFilterStack();
        this.title = filterStack.getHoverName().getString();
    }

    boolean closeGUI() {
        return menu.getLocator().either().map(
                hand -> {
                    if (ClientUtil.getClientPlayer().getItemInHand(hand).getItem() instanceof ModuleItem) {
                        // need to re-open module GUI for module in player's hand
                        ClientPacketDistributor.sendToServer(OpenGuiMessage.openModuleInHand(menu.getLocator()));
                        return true;
                    }
                    return false;
                },
                _ -> {
                    ClientPacketDistributor.sendToServer(OpenGuiMessage.openModuleInRouter(menu.getLocator()));
                    return true;
                });
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
    public void resync(ItemStack stack) {
        // nothing by default
    }
}
