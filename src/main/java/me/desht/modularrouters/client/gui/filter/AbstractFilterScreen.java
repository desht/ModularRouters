package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.IResyncableGui;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.network.messages.OpenGuiMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractFilterScreen extends Screen implements IResyncableGui {
    protected final Component title;
    protected final ItemStack filterStack;
    final MFLocator locator;

    AbstractFilterScreen(ItemStack filterStack, MFLocator locator) {
        super(filterStack.getHoverName());

        this.filterStack = filterStack;
        this.locator = locator;

        this.title = filterStack.getHoverName();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE || (ClientUtil.isInvKey(event.key())) /*&& (!hasTextFieldManager() || !getOrCreateTextFieldManager().isFocused())*/) {
            // Intercept ESC/<inv> and immediately reopen the previous GUI, if any
            if (closeGUI()) return true;
        }
        return super.keyPressed(event);
    }

    boolean closeGUI() {
        return locator.either().map(
                hand -> {
                    if (ClientUtil.getClientPlayer().getItemInHand(hand).getItem() instanceof ModuleItem) {
                        // need to re-open module GUI for module in player's hand
                        ClientPacketDistributor.sendToServer(OpenGuiMessage.openModuleInHand(locator));
                        return true;
                    }
                    return false;
                },
                _ -> {
                    ClientPacketDistributor.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
                    return true;
                }
        );
    }
}
