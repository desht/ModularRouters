package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.SorterExecutor;

public class ItemSorterModule extends AbstractModule {
    public ItemSorterModule() {
        super("sorterModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new SorterExecutor();
    }
}
