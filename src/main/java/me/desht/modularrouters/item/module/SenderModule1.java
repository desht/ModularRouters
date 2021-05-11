package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule1;
import net.minecraft.item.ItemStack;

public class SenderModule1 extends ItemModule implements IRangedModule {

    private static final TintColor TINT_COLOR = new TintColor(221, 255, 163);

    public SenderModule1() {
        super(ModItems.defaultProps(), CompiledSenderModule1::new);
    }

    @Override
    public int getBaseRange() {
        return MRConfig.Common.Module.sender1BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return MRConfig.Common.Module.sender1MaxRange;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return MRConfig.Common.EnergyCosts.senderModule1EnergyCost;
    }
}
