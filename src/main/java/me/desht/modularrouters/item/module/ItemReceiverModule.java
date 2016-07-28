package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.execution.ModuleExecutor;

public class ItemReceiverModule extends AbstractModule {
    public ItemReceiverModule() {
        super("receiverModule");
        setMaxDamage(0);
    }

    @Override
    public ModuleExecutor getExecutor() {
        // TODO: still not sure if this module is even needed
        return null;
    }
}
