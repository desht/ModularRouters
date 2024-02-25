package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.IResyncableGui;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.network.FilterOp;
import me.desht.modularrouters.network.messages.FilterSettingsMessage;
import me.desht.modularrouters.network.messages.OpenGuiMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractFilterScreen extends Screen implements IResyncableGui {
    protected final Component title;
    final MFLocator locator;

    AbstractFilterScreen(ItemStack filterStack, MFLocator locator) {
        super(filterStack.getHoverName());
        this.locator = locator;

        this.title = filterStack.getHoverName();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (ClientUtil.isInvKey(keyCode)) /*&& (!hasTextFieldManager() || !getOrCreateTextFieldManager().isFocused())*/) {
            // Intercept ESC/<inv> and immediately reopen the previous GUI, if any
            if (closeGUI()) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    boolean closeGUI() {
        if (locator.routerPos() != null) {
            // need to re-open module GUI for module in router slot <moduleSlotIndex>
            PacketDistributor.SERVER.noArg().send(OpenGuiMessage.openModuleInRouter(locator));
            return true;
        } else if (locator.hand() != null) {
            ItemStack stack = getMinecraft().player.getItemInHand(locator.hand());
            if (stack.getItem() instanceof ModuleItem) {
                // need to re-open module GUI for module in player's hand
                PacketDistributor.SERVER.noArg().send(OpenGuiMessage.openModuleInHand(locator));
                return true;
            }
        }
        return false;
    }

    void sendAddStringMessage(String key, String s) {
        CompoundTag ext = Util.make(new CompoundTag(), tag -> tag.putString(key, s));
        PacketDistributor.SERVER.noArg().send(new FilterSettingsMessage(FilterOp.ADD_STRING, locator, ext));
    }

    void sendRemovePosMessage(int pos) {
        CompoundTag ext = Util.make(new CompoundTag(), tag -> tag.putInt("Pos", pos));
        PacketDistributor.SERVER.noArg().send(new FilterSettingsMessage(FilterOp.REMOVE_AT, locator, ext));
    }
}
