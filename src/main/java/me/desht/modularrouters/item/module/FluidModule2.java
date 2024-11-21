package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class FluidModule2 extends FluidModule1 implements IRangedModule, IPositionProvider, ITargetedModule {
    private static final TintColor TINT_COLOR = new TintColor(64, 224, 255);

    @Override
    public boolean isValidTarget(UseOnContext ctx) {
        return !ctx.getLevel().isEmptyBlock(ctx.getClickedPos());
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.fluid2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.fluid2MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8040E0FF;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.fluidModule2EnergyCost.get();
    }

}
