package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public abstract class RangeAugments extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return ItemModule.getModule(moduleType) instanceof IRangedModule;
    }

    @Override
    public String getExtraInfo(int c, ItemStack stack) {
        Module m = ItemModule.getModule(stack);
        if (m instanceof IRangedModule) {
            IRangedModule r = (IRangedModule) m;
            return " - " + I18n.format("itemText.augments.rangeInfo", r.getCurrentRange(stack), r.getHardMaxRange());
        } else {
            return "";
        }
    }

    static class RangeUpAugment extends RangeAugments {
    }

    static class RangeDownAugment extends RangeAugments {
    }
}
