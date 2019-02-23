package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.IPositionProvider;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule2;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class SenderModule2 extends TargetedModule implements IRangedModule, IPositionProvider {
    public SenderModule2(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledSenderModule2(router, stack);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.MODULE.sender2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.MODULE.sender2MaxRange.get();
    }

    @Override
    public Color getItemTint() {
        return new Color(149, 255, 93);
    }

    @Override
    public List<ModuleTarget> getStoredPositions(@Nonnull ItemStack stack) {
        ModuleTarget target = TargetedModule.getTarget(stack);
        return target == null ? Collections.emptyList() : Collections.singletonList(target);
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8095FF5D;
    }
}
