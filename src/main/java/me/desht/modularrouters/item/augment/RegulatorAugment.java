package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.DetectorModule;
import me.desht.modularrouters.item.module.ExtruderModule2;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class RegulatorAugment extends AugmentItem {
    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return moduleType instanceof DetectorModule || moduleType instanceof ExtruderModule2 ? 0 : 1;
    }

    @Override
    public Component getExtraInfo(int c, ItemStack moduleStack) {
        int amount = ModuleHelper.getRegulatorAmount(moduleStack);
        String key = ((ModuleItem) moduleStack.getItem()).getRegulatorTranslationKey(moduleStack);
        return Component.literal(" - ").append(xlate(key, amount));
    }
}
