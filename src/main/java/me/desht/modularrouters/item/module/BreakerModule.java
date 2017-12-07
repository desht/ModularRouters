package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class BreakerModule extends Module {
    @Override
    public void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addUsageInformation(itemstack, player, list, advanced);
        Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(itemstack);
        if (ench.isEmpty()) {
            list.addAll(MiscUtil.wrapString(I18n.format("itemText.misc.enchantBreakerHint"), 45));
        }
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledBreakerModule(router, stack);
    }

    @Override
    public Color getItemTint() {
        return new Color(240, 208, 208);
    }
}
