package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.IResyncableGui;
import me.desht.modularrouters.client.gui.widgets.GuiScreenBase;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.FilterSettingsMessage.Operation;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public abstract class GuiFilterScreen extends GuiScreenBase implements IResyncableGui {
    protected final ITextComponent title;
    final MFLocator locator;

    GuiFilterScreen(ItemStack filterStack, MFLocator locator) {
        super(filterStack.getDisplayName());
        this.locator = locator;

        this.title = filterStack.getDisplayName();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (ClientUtil.isInvKey(keyCode)) && (!hasTextFieldManager() || !getOrCreateTextFieldManager().isFocused())) {
            // Intercept ESC/<inv> and immediately reopen the previous GUI, if any
            if (closeGUI()) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    boolean closeGUI() {
        if (locator.routerPos != null) {
            // need to re-open module GUI for module in router slot <moduleSlotIndex>
            PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInRouter(locator));
            return true;
        } else if (locator.hand != null) {
            ItemStack stack = getMinecraft().player.getHeldItem(locator.hand);
            if (stack.getItem() instanceof ItemModule) {
                // need to re-open module GUI for module in player's hand
                PacketHandler.NETWORK.sendToServer(OpenGuiMessage.openModuleInHand(locator));
                return true;
            }
        }
        return false;
    }

    void sendAddStringMessage(String key, String s) {
        CompoundNBT ext = new CompoundNBT();
        ext.putString(key, s);
        PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.ADD_STRING, locator, ext));
    }

    void sendRemovePosMessage(int pos) {
        CompoundNBT ext = new CompoundNBT();
        ext.putInt("Pos", pos);
        PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(Operation.REMOVE_AT, locator, ext));
    }
}
