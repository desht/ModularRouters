package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class RedstoneAugment extends AugmentItem {
    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return 1;
    }

    @Override
    public Component getExtraInfo(int c, ItemStack moduleStack) {
        RouterRedstoneBehaviour rrb = ModuleHelper.getRedstoneBehaviour(moduleStack);
        return Component.literal(" - ").append(xlate("modularrouters.guiText.tooltip.redstone." + rrb.toString()));
    }
}
