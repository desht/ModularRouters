package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPlacerModule;
import me.desht.modularrouters.util.TintColor;
import net.minecraft.item.ItemStack;

public class PlacerModule extends ItemModule {
    public PlacerModule(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPlacerModule(router, stack);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(240, 208, 208);
    }
}
