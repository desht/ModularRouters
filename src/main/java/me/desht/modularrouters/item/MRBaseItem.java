package me.desht.modularrouters.item;

import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public abstract class MRBaseItem extends Item {
    public MRBaseItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        if (world == null) return;

        Component text = ClientSetup.keybindModuleInfo.getTranslatedKeyMessage();

        if (ClientSetup.keybindModuleInfo.isDown()) {
            addUsageInformation(stack, list);
        } else if (MRConfig.Client.Misc.alwaysShowModuleSettings || Screen.hasShiftDown()) {
            addExtraInformation(stack, list);
            if (ClientUtil.thisScreenPassesEvents()) {
                list.add(xlate("modularrouters.itemText.misc.holdKey", text.getString()));
            }
        } else if (!MRConfig.Client.Misc.alwaysShowModuleSettings) {
            if (ClientUtil.thisScreenPassesEvents()) {
                list.add(xlate("modularrouters.itemText.misc.holdShiftKey", text.getString()));
            }
        }
    }

    protected void addUsageInformation(ItemStack itemstack, List<Component> list) {
        MiscUtil.appendMultilineText(list, ChatFormatting.GRAY,
                "modularrouters.itemText.usage.item." + itemstack.getItem().getRegistryName().getPath(), getExtraUsageParams());
    }

    protected abstract void addExtraInformation(ItemStack stack, List<Component> list);

    protected Object[] getExtraUsageParams() {
        return new Object[0];
    }
}
