package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.FluidModule;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class RegulatorAugment extends ItemAugment {

    public RegulatorAugment(Properties props) {
        super(props);
    }

    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return !(moduleType instanceof DetectorModule || moduleType instanceof ExtruderModule2);
    }

    @Override
    public String getExtraInfo(int c, ItemStack moduleStack) {
        int amount = ModuleHelper.getRegulatorAmount(moduleStack);
        String s = moduleStack.getItem() instanceof FluidModule ? "labelFluid" : "label";
        return " - " + I18n.format("guiText.tooltip.regulator." + s, amount);
    }
}
