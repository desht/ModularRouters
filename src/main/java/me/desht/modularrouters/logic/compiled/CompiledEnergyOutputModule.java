package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CompiledEnergyOutputModule extends CompiledModule {
    public CompiledEnergyOutputModule(@Nullable TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        if (getTarget() == null) return false;

        return router.getCapability(CapabilityEnergy.ENERGY)
                .map(routerHandler -> getTarget().getEnergyHandler().map(otherHandler -> {
                    int toExtract = routerHandler.extractEnergy(router.getEnergyXferRate(), true);
                    int inserted = otherHandler.receiveEnergy(toExtract, false);
                    routerHandler.extractEnergy(inserted, false);
                    return inserted > 0;
                }).orElse(false))
                .orElse(false);
    }

}
