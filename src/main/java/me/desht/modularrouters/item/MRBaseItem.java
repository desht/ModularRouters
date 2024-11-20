package me.desht.modularrouters.item;

import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.ConfigHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public abstract class MRBaseItem extends Item {
    public MRBaseItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        if (context.registries() == null) return;

        MutableComponent text = ClientSetup.keybindModuleInfo.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.DARK_AQUA);

        if (ClientUtil.isKeyDown(ClientSetup.keybindModuleInfo)) {
            addUsageInformation(stack, list);
        } else if (ConfigHolder.client.misc.alwaysShowModuleSettings.get() || Screen.hasShiftDown()) {
            addExtraInformation(stack, list);
            list.add(Component.empty());
            list.add(xlate("modularrouters.itemText.misc.holdKey", text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (!ConfigHolder.client.misc.alwaysShowModuleSettings.get()) {
            list.add(Component.empty());
            Component shift = Component.literal("Shift").withStyle(ChatFormatting.DARK_AQUA);
            list.add(xlate("modularrouters.itemText.misc.holdShiftKey", shift, text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    protected void addUsageInformation(ItemStack itemstack, List<Component> list) {
        var key = BuiltInRegistries.ITEM.getKey(itemstack.getItem());
        list.add(xlate(key.getNamespace() + ".itemText.usage.item." + key.getPath(), getExtraUsageParams()));
    }

    protected abstract void addExtraInformation(ItemStack stack, List<Component> list);

    protected Object[] getExtraUsageParams() {
        return new Object[0];
    }
}
