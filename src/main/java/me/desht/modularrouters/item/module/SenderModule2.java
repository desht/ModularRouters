package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule2;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

public class SenderModule2 extends TargetedModule implements IRangedModule, IPositionProvider {

    private static final TintColor TINT_COLOR = new TintColor(149, 255, 93);

    public SenderModule2() {
        super(ModItems.defaultProps(), CompiledSenderModule2::new);
    }

    public SenderModule2(BiFunction<ModularRouterBlockEntity,ItemStack,? extends CompiledModule> compiler) {
        super(ModItems.defaultProps(), compiler);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.sender2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.sender2MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8095FF5D;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.senderModule2EnergyCost.get();
    }
}
