package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule1;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Map;

public class ExtruderModule1 extends ItemModule implements IRangedModule {
    public ExtruderModule1() {
        super(ModItems.defaultProps());
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruderModule1(router, stack);
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addUsageInformation(itemstack, list);
        Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(itemstack);
        if (ench.isEmpty()) {
            list.add(new TranslationTextComponent("itemText.misc.enchantBreakerHint"));
        }
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);
        list.add(new TranslationTextComponent("itemText.extruder.mode." + ModuleHelper.getRedstoneBehaviour(itemstack)));
    }

    @Override
    public int getBaseRange() {
        return MRConfig.Common.Module.extruder1BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return MRConfig.Common.Module.extruder1MaxRange;
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(227, 174, 27);
    }
}
