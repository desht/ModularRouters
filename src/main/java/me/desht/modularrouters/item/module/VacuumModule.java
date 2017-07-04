package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class VacuumModule extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVacuumModule(router, stack);
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.module.vacuumBaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.module.vacuumMaxRange;
    }

    @Override
    public boolean isOmniDirectional() {
        return true;
    }

    @Override
    public Color getItemTint() {
        return new Color(120, 48, 191);
    }
}
