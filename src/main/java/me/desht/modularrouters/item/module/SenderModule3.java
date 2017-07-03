package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule3;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class SenderModule3 extends TargetedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule3(router, stack);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    protected boolean isRangeLimited() {
        return false;
    }

    @Override
    public Color getItemTint() {
        return new Color(25, 255, 11);
    }
}
