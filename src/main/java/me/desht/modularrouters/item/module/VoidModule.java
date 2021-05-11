package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledVoidModule;

public class VoidModule extends ItemModule {

    private static final TintColor TINT_COLOR = new TintColor(255, 0, 0);

    public VoidModule() {
        super(ModItems.defaultProps(), CompiledVoidModule::new);
    }


    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost() {
        return MRConfig.Common.EnergyCosts.voidModuleEnergyCost;
    }
}
