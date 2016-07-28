package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.PlacerExecutor;

public class ItemPlacerModule extends AbstractModule {
    public ItemPlacerModule() {
        super("placerModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new PlacerExecutor();
    }
}
