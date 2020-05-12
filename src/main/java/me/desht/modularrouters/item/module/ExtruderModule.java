package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ExtruderModule extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruderModule(router, stack);
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addUsageInformation(itemstack, player, list, advanced);
        Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(itemstack);
        if (ench.isEmpty()) {
            list.addAll(MiscUtil.wrapString(I18n.format("itemText.misc.enchantBreakerHint")));
        }
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);

        ItemStack pick = ModuleHelper.getPickaxe(itemstack);
        list.add(I18n.format("itemText.misc.breakerModulePick", pick.getDisplayName()));
        EnchantmentHelper.getEnchantments(pick).forEach((ench, level) -> {
            list.add(TextFormatting.YELLOW + "\u25b6 " + TextFormatting.AQUA + ench.getTranslatedName(level));
        });

        list.add(I18n.format("itemText.extruder.mode." + ModuleHelper.getRedstoneBehaviour(itemstack)));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        ItemStack pick = ModuleHelper.getPickaxe(stack);
        return !EnchantmentHelper.getEnchantments(pick).isEmpty();
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.module.extruderBaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.module.extruderMaxRange;
    }

    @Override
    public Color getItemTint() {
        return new Color(227, 174, 27);
    }
}
