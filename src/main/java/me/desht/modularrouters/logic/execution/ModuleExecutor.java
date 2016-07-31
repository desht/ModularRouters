package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;

public abstract class ModuleExecutor {
    public abstract boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings);
}
