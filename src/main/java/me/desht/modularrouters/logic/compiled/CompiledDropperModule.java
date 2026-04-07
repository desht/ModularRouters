package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.PickupDelayAugment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class CompiledDropperModule extends CompiledModule {
    private final int pickupDelay;  // ticks

    public CompiledDropperModule(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        pickupDelay = getAugmentCount(ModItems.PICKUP_DELAY_AUGMENT) * PickupDelayAugment.TICKS_PER_AUGMENT;
    }

    @Override
    public boolean execute(ModularRouterBlockEntity router) {
        ItemStack stack = router.getBufferItemStack();
        if (getFilter().test(stack) && isRegulationOK(router, false)) {
            int nItems = Math.min(getItemsPerTick(router), stack.getCount() - getRegulationAmount());
            if (nItems <= 0) {
                return false;
            }
            ItemStack toDrop = router.peekBuffer(nItems);
            return getTarget().map(target -> {
                BlockPos pos = target.gPos.pos();
                Direction face = target.face;
                ItemEntity item = new ItemEntity(router.nonNullLevel(),
                        pos.getX() + 0.5 + 0.2 * face.getStepX(),
                        pos.getY() + 0.5 + 0.2 * face.getStepY(),
                        pos.getZ() + 0.5 + 0.2 * face.getStepZ(),
                        toDrop);
                item.setPickUpDelay(pickupDelay);
                setupItemVelocity(router, item);
                router.nonNullLevel().addFreshEntity(item);
                router.extractBuffer(toDrop.getCount());
                return true;
            }).orElse(false);
        } else {
            return false;
        }
    }

    void setupItemVelocity(ModularRouterBlockEntity router, ItemEntity item) {
        item.setDeltaMovement(0, 0, 0);
    }
}
