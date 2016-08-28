package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

public class CompiledBreakerModuleSettings extends CompiledModuleSettings {
    private final boolean silkTouch;
    private final int fortune;

    public CompiledBreakerModuleSettings(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
        fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
    }

    public boolean isSilkTouch() {
        return silkTouch;
    }

    public int getFortune() {
        return fortune;
    }
}
