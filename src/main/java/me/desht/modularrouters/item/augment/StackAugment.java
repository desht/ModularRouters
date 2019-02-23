package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class StackAugment extends ItemAugment {
    public StackAugment(Properties props) {
        super(props);
    }

    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return !(moduleType instanceof DetectorModule || moduleType instanceof ExtruderModule1 || moduleType instanceof ExtruderModule2
                || moduleType instanceof BreakerModule || moduleType instanceof PlacerModule || moduleType instanceof FluidModule);
    }

    @Override
    public String getExtraInfo(int c, ItemStack stack) {
        return " - " + I18n.format("itemText.augments.stackInfo", Math.min(1 << c, 64));
    }
}
