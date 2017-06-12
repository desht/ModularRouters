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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
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
        return new Object[]{ConfigHandler.module.extruderBaseRange, ConfigHandler.module.extruderMaxRange};
    }

    @Override
    protected void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addUsageInformation(itemstack, player, list, advanced);
        Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(itemstack);
        if (ench.isEmpty()) {
            MiscUtil.appendMultiline(list, "itemText.misc.enchantExtruderHint");
        }
    }

    @Override
    protected void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        list.add(I18n.format("itemText.extruder.mode." + ModuleHelper.getRedstoneBehaviour(itemstack)));
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.EXTRUDER),
                ModuleHelper.makeItemStack(ItemModule.ModuleType.PLACER),
                Items.REDSTONE,
                ModuleHelper.makeItemStack(ItemModule.ModuleType.BREAKER));
    }

    public static int maxDistance(TileEntityItemRouter router) {
        return router == null ?
                ConfigHandler.module.extruderBaseRange :
                router.getEffectiveRange(ConfigHandler.module.extruderBaseRange, 1, ConfigHandler.module.extruderMaxRange);
    }
}
