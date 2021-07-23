package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

public class StackAugment extends AugmentItem {
    @Override
    public int getMaxAugments(ModuleItem moduleType) {
        return moduleType instanceof DetectorModule
                || moduleType instanceof ExtruderModule1 || moduleType instanceof ExtruderModule2
                || moduleType instanceof BreakerModule || moduleType instanceof PlacerModule
                || moduleType instanceof FluidModule1 || moduleType instanceof FluidModule2 ? 0 : 6;
    }

    @Override
    public String getExtraInfo(int c, ItemStack stack) {
        return " - " + I18n.get("modularrouters.itemText.augments.stackInfo", Math.min(1 << c, 64));
    }
}
