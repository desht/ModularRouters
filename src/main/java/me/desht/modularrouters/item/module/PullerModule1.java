package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class PullerModule1 extends ItemModule {
    public PullerModule1(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPullerModule(router, stack);
    }

    @Override
    public Color getItemTint() {
        return new Color(192, 192, 255);
    }
}
