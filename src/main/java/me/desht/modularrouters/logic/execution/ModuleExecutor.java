package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;

public abstract class ModuleExecutor {
    public abstract boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings);
}
