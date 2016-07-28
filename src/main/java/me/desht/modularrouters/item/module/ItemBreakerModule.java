package me.desht.modularrouters.item.module;

import me.desht.modularrouters.logic.CompiledBreakerModuleSettings;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.BreakerExecutor;

public class ItemBreakerModule extends AbstractModule {
    public ItemBreakerModule() {
        super("breakerModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new BreakerExecutor();
    }

    @Override
    public Class<? extends CompiledModuleSettings> getCompiler() {
        return CompiledBreakerModuleSettings.class;
    }
}
