package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.widgets.GuiScreenBase;
import me.desht.modularrouters.client.gui.widgets.IResyncableGui;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.OpenGuiMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public abstract class GuiFilterScreen extends GuiScreenBase implements IResyncableGui {
    protected final BlockPos routerPos;
    protected final Integer moduleSlotIndex;  // slot of the module in the router
    protected final Integer filterSlotIndex;  // slot of the filter item in the module
    protected final EnumHand hand;
    protected final String title;

    GuiFilterScreen(ItemStack filterStack, BlockPos routerPos, Integer moduleSlotIndex, Integer filterSlotIndex, EnumHand hand) {
        this.routerPos = routerPos;
        this.moduleSlotIndex = moduleSlotIndex;
        this.filterSlotIndex = filterSlotIndex;
        this.hand = hand;
        this.title = filterStack.getDisplayName();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || (keyCode == Keyboard.KEY_E) && (!hasTextFieldManager() || !getTextFieldManager().isFocused())) {
            // Intercept ESC/E and immediately reopen the previous GUI, if any
            if (closeGUI()) return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    boolean closeGUI() {
        if (routerPos != null) {
            // need to re-open module GUI for module in router slot <moduleSlotIndex>
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(mc.world, routerPos);
            if (router != null) {
                router.playerConfiguringModule(mc.player, moduleSlotIndex);
                ModularRouters.network.sendToServer(OpenGuiMessage.openModuleInRouter(routerPos, moduleSlotIndex));
                return true;
            }
        } else if (hand != null) {
            ItemStack stack = mc.player.getHeldItem(hand);
            if (ItemModule.getModule(stack) != null) {
                // need to re-open module GUI for module in player's hand
                ModularRouters.network.sendToServer(OpenGuiMessage.openModuleInHand(hand));
                return true;
            }
        }
        return false;
    }

    void sendAddStringMessage(String key, String s) {
        NBTTagCompound ext = new NBTTagCompound();
        ext.setString(key, s);
        if (routerPos != null) {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.ADD_STRING, routerPos, moduleSlotIndex, filterSlotIndex, ext));
        } else {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.ADD_STRING, hand, filterSlotIndex, ext));
        }
    }

    void sendRemovePosMessage(int pos) {
        NBTTagCompound ext = new NBTTagCompound();
        ext.setInteger("Pos", pos);
        if (routerPos != null) {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.REMOVE_AT, routerPos, moduleSlotIndex, filterSlotIndex, ext));
        } else {
            ModularRouters.network.sendToServer(new FilterSettingsMessage(
                    FilterSettingsMessage.Operation.REMOVE_AT, hand, filterSlotIndex, ext));
        }
    }
}
