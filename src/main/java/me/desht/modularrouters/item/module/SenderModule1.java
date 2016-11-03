package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule1;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class SenderModule1 extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule1(router, stack);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] { Config.Defaults.SENDER1_BASE_RANGE, Config.Defaults.SENDER1_MAX_RANGE };
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.SENDER1),
                ModItems.blankModule, Items.BOW, Items.ARROW);
    }

    public static int maxDistance(TileEntityItemRouter router) {
        return Math.min(Config.sender1MaxRange, Config.sender1BaseRange + router.getRangeUpgrades());
    }
}
