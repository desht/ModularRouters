package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule1;

public class PullerModule1 extends ItemModule {

    private static final TintColor TINT_COLOR = new TintColor(192, 192, 255);

    public PullerModule1() {
        super(ModItems.defaultProps(), CompiledPullerModule1::new);
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost() {
        return MRConfig.Common.EnergyCosts.pullerModule1EnergyCost;
    }
}
