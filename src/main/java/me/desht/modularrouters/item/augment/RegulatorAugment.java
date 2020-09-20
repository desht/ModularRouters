package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class RegulatorAugment extends ItemAugment {
    @Override
    public int getMaxAugments(ItemModule moduleType) {
        return moduleType instanceof DetectorModule || moduleType instanceof ExtruderModule2 ? 0 : 1;
    }

    @Override
    public String getExtraInfo(int c, ItemStack moduleStack) {
        int amount = ModuleHelper.getRegulatorAmount(moduleStack);
        String key = ((ItemModule) moduleStack.getItem()).getRegulatorTranslationKey(moduleStack);
        return " - " + I18n.format(key, amount);
    }
}
