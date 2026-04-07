package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import org.jspecify.annotations.Nullable;

public class CompiledEnergyOutputModule extends CompiledModule {
    public CompiledEnergyOutputModule(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(ModularRouterBlockEntity router) {
        return getTarget().map(target -> {
            EnergyHandler routerStorage = router.getEnergyStorage();
            EnergyHandler otherStorage = target.getEnergyHandler().orElse(null);

            if (routerStorage.getCapacityAsInt() > 0 && otherStorage != null) {
                int transferred = EnergyHandlerUtil.move(routerStorage, otherStorage, router.getEnergyXferRate(), null);
                return transferred > 0;
            }

            return false;
        }).orElse(false);
    }
}
