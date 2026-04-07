package me.desht.modularrouters.integration.ffs;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.api.matching.IModuleFlags;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

public class FFSItemMatcher implements IItemMatcher {
    private final ItemStack ffsFilterStack;

    public FFSItemMatcher(ItemStack stack) {
        Validate.isTrue(FTBFilterSystemAPI.api().isFilterItem(stack));
        this.ffsFilterStack = stack;
    }

    @Override
    public boolean matchItem(ItemStack stack, IModuleFlags flags, HolderLookup.@Nullable Provider registryAccess) {
        return registryAccess != null && FTBFilterSystemAPI.api().doesFilterMatch(ffsFilterStack, stack, registryAccess);
    }
}
