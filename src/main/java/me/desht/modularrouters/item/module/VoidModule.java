package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledVoidModule;
import me.desht.modularrouters.util.TintColor;
import net.minecraft.item.ItemStack;

public class VoidModule extends ItemModule {
    public VoidModule(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVoidModule(router, stack);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(255, 0, 0);
    }
}
