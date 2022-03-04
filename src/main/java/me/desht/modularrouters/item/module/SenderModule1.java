package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule1;
import net.minecraft.world.item.ItemStack;

public class SenderModule1 extends ModuleItem implements IRangedModule {

    private static final TintColor TINT_COLOR = new TintColor(221, 255, 163);

    public SenderModule1() {
        super(ModItems.defaultProps(), CompiledSenderModule1::new);
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.sender1BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.sender1MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.senderModule1EnergyCost.get();
    }
}
