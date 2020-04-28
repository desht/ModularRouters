package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;

public class BreakerModule extends ItemModule implements IPickaxeUser {
    public BreakerModule() {
        super(ModItems.defaultProps());
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledBreakerModule(router, stack);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(240, 208, 208);
    }

}
