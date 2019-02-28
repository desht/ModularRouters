package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.widgets.GuiScreenBase;
import me.desht.modularrouters.client.gui.widgets.IResyncableGui;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.OpenGuiMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import org.lwjgl.glfw.GLFW;

public abstract class GuiFilterScreen extends GuiScreenBase implements IResyncableGui {
    protected final TileEntityItemRouter router;
    protected final EnumHand hand;
    protected final String title;

    GuiFilterScreen(ItemStack filterStack, TileEntityItemRouter router, EnumHand hand) {
        this.router = router;
        this.hand = hand;
        this.title = filterStack.getDisplayName().getString();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (keyCode == GLFW.GLFW_KEY_E) && (!hasTextFieldManager() || !getTextFieldManager().isFocused())) {
            // Intercept ESC/E and immediately reopen the previous GUI, if any
            if (closeGUI()) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    boolean closeGUI() {
        SlotTracker.getInstance(mc.player).clearFilterSlot();
        if (router != null) {
            // need to re-open module GUI for module in router slot <moduleSlotIndex>
//          router.playerConfiguringModule(mc.player, moduleSlotIndex);
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

    void sendAddStringMessage(String key, String s) {
        NBTTagCompound ext = new NBTTagCompound();
        ext.putString(key, s);
        if (router != null) {
            PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.ADD_STRING, router.getPos(), ext));
        } else {
            PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.ADD_STRING, hand, ext));
        }
    }

    void sendRemovePosMessage(int pos) {
        NBTTagCompound ext = new NBTTagCompound();
        ext.putInt("Pos", pos);
        if (router != null) {
            PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.REMOVE_AT, router.getPos(), ext));
        } else {
            PacketHandler.NETWORK.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.REMOVE_AT, hand, ext));
        }
    }
}
