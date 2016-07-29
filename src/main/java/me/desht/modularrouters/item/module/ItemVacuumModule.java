package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.VacuumExecutor;

public class ItemVacuumModule extends AbstractModule {
    public ItemVacuumModule() {
        super("vacuumModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new VacuumExecutor();
    }

    public static int getVacuumRange(TileEntityItemRouter router) {
        return Math.min(Config.vacuumBaseRange + router.getRangeUpgrades(), Config.vacuumMaxRange);
    }
}
