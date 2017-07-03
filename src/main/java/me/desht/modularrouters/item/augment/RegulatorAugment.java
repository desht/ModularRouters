package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.FluidModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class RegulatorAugment extends Augment {
    @Override
    public boolean isCompatible(ItemModule.ModuleType moduleType) {
        return moduleType != ItemModule.ModuleType.DETECTOR && moduleType != ItemModule.ModuleType.EXTRUDER2;
    }

    @Override
    public String getExtraInfo(int c, ItemStack stack) {
        int amount = ModuleHelper.getRegulatorAmount(stack);
        String s = ItemModule.getModule(stack) instanceof FluidModule ? "labelFluid" : "label";
        return " - " + I18n.format("guiText.tooltip.regulator." + s, amount);
    }
}
