package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule1;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class SenderModule1 extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule1(router, stack);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] { Config.sender1BaseRange, Config.sender1MaxRange };
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.SENDER1),
                ModItems.blankModule, Items.BOW, Items.ARROW);
    }

    public static int maxDistance(TileEntityItemRouter router) {
        return router == null ? Config.sender1BaseRange : router.getEffectiveRange(Config.sender1BaseRange, 1, Config.sender1MaxRange);
    }
}
