package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class RegulatorUpgradeRecipe extends ModuleUpgradeRecipe {
    public RegulatorUpgradeRecipe(ItemStack result, Object... recipe) {
        super(result, recipe);
    }

    @Override
    public void enableUpgrade(ItemStack stack) {
        ModuleHelper.setRegulatorAmount(stack, true, 1);
    }

    public static boolean appliesTo(ItemModule.ModuleType type) {
        switch (type) {
            case SENDER1: case SENDER2: case SENDER3:
            case PULLER: case PLAYER:
                return true;
        }
        return false;
    }
}
