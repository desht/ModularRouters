package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class RedstoneAugment extends ItemAugment {
    @Override
    public int getMaxAugments(ItemModule moduleType) {
        return 1;
    }

    @Override
    public String getExtraInfo(int c, ItemStack moduleStack) {
        RouterRedstoneBehaviour rrb = ModuleHelper.getRedstoneBehaviour(moduleStack);
        return " - " + I18n.get("modularrouters.guiText.tooltip.redstone." + rrb.toString());
    }
}
