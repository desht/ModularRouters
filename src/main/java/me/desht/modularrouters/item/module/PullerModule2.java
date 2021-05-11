package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule2;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class PullerModule2 extends TargetedModule implements IRangedModule, IPositionProvider {

    private static final TintColor TINT_COLOR = new TintColor(128, 128, 255);

    public PullerModule2() {
        super(ModItems.defaultProps(), CompiledPullerModule2::new);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int getBaseRange() {
        return MRConfig.Common.Module.puller2BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return MRConfig.Common.Module.puller2MaxRange;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
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

    @Override
    public int getEnergyCost() {
        return MRConfig.Common.EnergyCosts.pullerModule2EnergyCost;
    }
}
