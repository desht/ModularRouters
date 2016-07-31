package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.CompiledBreakerModuleSettings;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.logic.execution.BreakerExecutor;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class ItemBreakerModule extends AbstractModule {
    public ItemBreakerModule() {
        super("breakerModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new BreakerExecutor();
    }

    @Override
    public Class<? extends CompiledModuleSettings> getCompiler() {
        return CompiledBreakerModuleSettings.class;
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addUsageInformation(itemstack, player, list, par4);
        Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(itemstack);
        if (ench.isEmpty()) {
            MiscUtil.appendMultiline(list, "itemText.misc.enchantBreakerHint");
        }
    }
}
