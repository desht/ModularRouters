package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule2;
import me.desht.modularrouters.util.TintColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PullerModule2 extends TargetedModule implements IRangedModule, IPositionProvider {
    public PullerModule2(Properties props) {
        super(props);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPullerModule2(router, stack);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.MODULE.puller2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.MODULE.puller2MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(128, 128, 255);
    }

    @Override
    public List<ModuleTarget> getStoredPositions(@Nonnull ItemStack stack) {
        ModuleTarget target = TargetedModule.getTarget(stack);
        return target == null ? Collections.emptyList() : Collections.singletonList(target);
    }

    @Override
    public int getRenderColor(int index) {
        return 0x808080FF;
    }
}
