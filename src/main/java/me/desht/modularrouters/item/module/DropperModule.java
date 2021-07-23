package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledDropperModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiFunction;

public class DropperModule extends ModuleItem {
    private static final TintColor TINT_COLOR = new TintColor(230, 204, 240);

    public DropperModule() {
        super(ModItems.defaultProps(), CompiledDropperModule::new);
    }

    public DropperModule(BiFunction<ModularRouterBlockEntity,ItemStack,? extends CompiledModule> compiler) {
        super(ModItems.defaultProps(), compiler);
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return MRConfig.Common.EnergyCosts.dropperModuleEnergyCost;
    }
}
