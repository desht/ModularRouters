package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledEnergyOutputModule;

public class EnergyOutputModule extends ItemModule {
    private static final TintColor TINT_COLOR = new TintColor(192, 192, 192);

    public EnergyOutputModule() {
        super(ModItems.defaultProps(), CompiledEnergyOutputModule::new);
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost() {
        return MRConfig.Common.EnergyCosts.energyoutputModuleEnergyCost;
    }

}
