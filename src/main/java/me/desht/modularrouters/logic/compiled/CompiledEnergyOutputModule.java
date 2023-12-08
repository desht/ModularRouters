package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompiledEnergyOutputModule extends CompiledModule {
    public CompiledEnergyOutputModule(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (getTarget() == null) return false;

        IEnergyStorage routerStorage = router.getEnergyStorage();
        IEnergyStorage otherStorage = getTarget().getEnergyHandler().orElse(null);

        if (routerStorage != null && otherStorage != null) {
            int toExtract = routerStorage.extractEnergy(router.getEnergyXferRate(), true);
            int inserted = otherStorage.receiveEnergy(toExtract, false);
            routerStorage.extractEnergy(inserted, false);
            return inserted > 0;
        }

        return false;
    }
}
