package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledDropperModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class DropperModule extends ItemModule {
    public DropperModule(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledDropperModule(router, stack);
    }

    @Override
    public Color getItemTint() {
        return new Color(230, 204, 240);
    }
}
