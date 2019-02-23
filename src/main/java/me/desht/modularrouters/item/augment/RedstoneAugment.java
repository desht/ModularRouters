package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class RedstoneAugment extends ItemAugment {
    public RedstoneAugment(Properties props) {
        super(props);
    }

    @Override
    public boolean isCompatible(ItemModule moduleType) {
        return true;
    }

    @Override
    public String getExtraInfo(int c, ItemStack moduleStack) {
        RouterRedstoneBehaviour rrb = ModuleHelper.getRedstoneBehaviour(moduleStack);
        return " - " + I18n.format("guiText.tooltip.redstone." + rrb.toString());
    }
}
