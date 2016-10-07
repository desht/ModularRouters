package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class VacuumModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVacuumModule(router, stack);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] { Config.Defaults.VACUUM_BASE_RANGE, Config.Defaults.VACUUM_MAX_RANGE };
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.VACUUM),
                ModItems.blankModule, Blocks.HOPPER, Items.ENDER_EYE);
    }

    public static int getVacuumRange(TileEntityItemRouter router) {
        return Math.min(Config.vacuumBaseRange + router.getRangeUpgrades(), Config.vacuumMaxRange);
    }
}
