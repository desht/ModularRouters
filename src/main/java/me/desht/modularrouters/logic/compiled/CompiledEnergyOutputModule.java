package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompiledEnergyOutputModule extends CompiledModule {
    public CompiledEnergyOutputModule(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (getTarget() == null) return false;

        EnergyHandler routerStorage = router.getEnergyStorage();
        EnergyHandler otherStorage = getTarget().getEnergyHandler().orElse(null);

        if (routerStorage != null && otherStorage != null) {
            int transferred = EnergyHandlerUtil.move(routerStorage, otherStorage, router.getEnergyXferRate(), null);
            return transferred > 0;
        }

        return false;
    }
}
