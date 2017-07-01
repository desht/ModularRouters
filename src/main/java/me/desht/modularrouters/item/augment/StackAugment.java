package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class StackAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return moduleType != ItemModule.ModuleType.DETECTOR
                && moduleType != ItemModule.ModuleType.EXTRUDER
                && moduleType != ItemModule.ModuleType.EXTRUDER2
                && moduleType != ItemModule.ModuleType.BREAKER
                && moduleType != ItemModule.ModuleType.PLACER
                && moduleType != ItemModule.ModuleType.FLUID;
    }

    @Override
    public String getExtraInfo(int c, ItemStack stack) {
        return " - " + I18n.format("itemText.augments.stackInfo", Math.min(1 << c, 64));
    }
}
