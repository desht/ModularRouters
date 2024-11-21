package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledPullerModule2;
import net.minecraft.world.item.ItemStack;

public class PullerModule2 extends ModuleItem implements IRangedModule, IPositionProvider, ITargetedModule {
    private static final TintColor TINT_COLOR = new TintColor(128, 128, 255);

    public PullerModule2() {
        super(ModItems.moduleProps(), CompiledPullerModule2::new);
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.puller2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.puller2MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getRenderColor(int index) {
        return 0x808080FF;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.pullerModule2EnergyCost.get();
    }
}
