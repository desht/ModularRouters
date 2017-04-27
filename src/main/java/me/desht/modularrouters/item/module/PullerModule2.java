package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule2;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class PullerModule2 extends TargetedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPullerModule2(router, stack);
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ModuleHelper.makeItemStack(ItemModule.ModuleType.PULLER2),
                ModuleHelper.makeItemStack(ItemModule.ModuleType.PULLER), Items.ENDER_EYE);
    }

    @Override
    public Object[] getExtraUsageParams() {
        return new Object[]{ConfigHandler.module.puller2BaseRange, ConfigHandler.module.puller2MaxRange};
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int maxDistanceSq(TileEntityItemRouter router) {
        int r = router == null ?
                ConfigHandler.module.puller2BaseRange :
                router.getEffectiveRange(ConfigHandler.module.puller2BaseRange, 1, ConfigHandler.module.puller2MaxRange);
        return r * r;
    }
}
