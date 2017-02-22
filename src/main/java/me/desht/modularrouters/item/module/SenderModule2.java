package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule2;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class SenderModule2 extends TargetedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule2(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.SENDER2),
                ModuleHelper.makeItemStack(ItemModule.ModuleType.SENDER1), Items.ENDER_EYE);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[]{Config.sender2BaseRange, Config.sender2MaxRange};
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int maxDistanceSq(TileEntityItemRouter router) {
        // TODO precalculate to avoid repeated multiplications
        int r = Math.min(Config.sender2BaseRange + (router == null ? 0 : router.getUpgradeCount(ItemUpgrade.UpgradeType.RANGE)), Config.sender2MaxRange);
        return r * r;
    }
}
