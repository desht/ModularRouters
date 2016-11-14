package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;
import java.util.Map;

public class ExtruderModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruderModule(router, stack);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[]{Config.Defaults.EXTRUDER_BASE_RANGE, Config.Defaults.EXTRUDER_MAX_RANGE};
    }

    @Override
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addUsageInformation(itemstack, player, list, par4);
        Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(itemstack);
        if (ench.isEmpty()) {
            MiscUtil.appendMultiline(list, "itemText.misc.enchantExtruderHint");
        }
    }

    @Override
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        list.add(I18n.format("itemText.extruder.mode." + ModuleHelper.getRedstoneBehaviour(stack)));
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.EXTRUDER),
                ItemModule.makeItemStack(ItemModule.ModuleType.PLACER),
                Items.REDSTONE,
                ItemModule.makeItemStack(ItemModule.ModuleType.BREAKER));
    }

    public static int maxDistance(TileEntityItemRouter router) {
        return Math.min(Config.extruderBaseRange + (router == null ? 0 : router.getUpgradeCount(ItemUpgrade.UpgradeType.RANGE)), Config.extruderMaxRange);
    }
}
