package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledEnergyOutputModule;
import net.minecraft.world.item.ItemStack;

public class EnergyOutputModule extends ItemModule {
    private static final TintColor TINT_COLOR = new TintColor(65, 4, 75);

    public EnergyOutputModule() {
        super(ModItems.defaultProps(), CompiledEnergyOutputModule::new);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(106, 12, 120);
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return MRConfig.Common.EnergyCosts.energyoutputModuleEnergyCost;
    }

}
