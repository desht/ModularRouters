package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule1;
import net.minecraft.item.ItemStack;

public class SenderModule1 extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule1(router, stack);
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.module.sender1BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.module.sender1MaxRange;
    }
}
