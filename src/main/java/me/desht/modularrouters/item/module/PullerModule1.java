package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule1;
import net.minecraft.item.ItemStack;

public class PullerModule1 extends ItemModule {
    public PullerModule1(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPullerModule1(router, stack);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(192, 192, 255);
    }
}
