package me.desht.modularrouters.logic.filter;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictMatcher {
    private final int[] ids;

    public OreDictMatcher(ItemStack stack) {
        ids = stack.isEmpty() ? new int[0] : OreDictionary.getOreIDs(stack); // paranoia
    }

    public boolean match(ItemStack stack) {
        if (ids.length == 0) return false;

        int[] oreIDs = OreDictionary.getOreIDs(stack);
        if (oreIDs.length == 0) return false;

        for (int i1 : oreIDs) {
            for (int i2 : ids) {
                if (i1 == i2) return true;
            }
        }

        return false;
    }
}
