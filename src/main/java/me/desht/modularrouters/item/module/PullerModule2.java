package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule2;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class PullerModule2 extends TargetedModule implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPullerModule2(router, stack);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.module.puller2BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.module.puller2MaxRange;
    }

    @Override
    public Color getItemTint() {
        return new Color(128, 134, 255);
    }
}
