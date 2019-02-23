package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule1;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class SenderModule1 extends ItemModule implements IRangedModule {
    public SenderModule1(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule1(router, stack);
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.MODULE.sender1BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.MODULE.sender1MaxRange.get();
    }

    @Override
    public Color getItemTint() {
        return new Color(221, 255, 163);
    }
}
