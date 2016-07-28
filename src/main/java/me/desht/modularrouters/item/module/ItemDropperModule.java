package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.execution.DropperExecutor;
import me.desht.modularrouters.logic.execution.ModuleExecutor;

public class ItemDropperModule extends AbstractModule {
    public ItemDropperModule() {
        super("dropperModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new DropperExecutor();
    }
}
