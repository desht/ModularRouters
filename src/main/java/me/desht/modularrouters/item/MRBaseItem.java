package me.desht.modularrouters.item;

import me.desht.modularrouters.client.KeyBindings;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.ConfigHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public abstract class MRBaseItem extends Item {
    public MRBaseItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        if (context.registries() == null) return;

        MutableComponent text = KeyBindings.keybindModuleInfo.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.DARK_AQUA);

        if (ClientUtil.isKeyDown(KeyBindings.keybindModuleInfo)) {
            addUsageInformation(stack, tooltipAdder);
        } else if (ConfigHolder.client.misc.alwaysShowModuleSettings.get() || Minecraft.getInstance().hasShiftDown()) {
            addExtraInformation(stack, tooltipAdder);
            tooltipAdder.accept(Component.empty());
            tooltipAdder.accept(xlate("modularrouters.itemText.misc.holdKey", text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (!ConfigHolder.client.misc.alwaysShowModuleSettings.get()) {
            tooltipAdder.accept(Component.empty());
            Component shift = Component.literal("Shift").withStyle(ChatFormatting.DARK_AQUA);
            tooltipAdder.accept(xlate("modularrouters.itemText.misc.holdShiftKey", shift, text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }


    protected void addUsageInformation(ItemStack itemstack, Consumer<Component> tooltipAdder) {
        var key = BuiltInRegistries.ITEM.getKey(itemstack.getItem());
        tooltipAdder.accept(xlate(key.getNamespace() + ".itemText.usage.item." + key.getPath(), getExtraUsageParams()));
    }

    protected abstract void addExtraInformation(ItemStack stack, Consumer<Component> tooltipAdder);

    protected Object[] getExtraUsageParams() {
        return new Object[0];
    }
}
