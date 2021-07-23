package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

public class RegulatorAugment extends AugmentItem {
    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return moduleType instanceof DetectorModule || moduleType instanceof ExtruderModule2 ? 0 : 1;
    }

    @Override
    public String getExtraInfo(int c, ItemStack moduleStack) {
        int amount = ModuleHelper.getRegulatorAmount(moduleStack);
        String key = ((ModuleItem) moduleStack.getItem()).getRegulatorTranslationKey(moduleStack);
        return " - " + I18n.get(key, amount);
    }
}
