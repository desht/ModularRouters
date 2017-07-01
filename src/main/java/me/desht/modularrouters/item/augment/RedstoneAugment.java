package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class RedstoneAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return true;
    }

    @Override
    public String getExtraInfo(int c, ItemStack stack) {
        RouterRedstoneBehaviour rrb = ModuleHelper.getRedstoneBehaviour(stack);
        return " - " + I18n.format("guiText.tooltip.redstone." + rrb.toString());
    }
}
