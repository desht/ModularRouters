package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.PullerExecutor;

public class ItemPullerModule extends AbstractModule {
    public ItemPullerModule() {
        super("pullerModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new PullerExecutor();
    }
}
