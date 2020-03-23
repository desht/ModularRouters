package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.PickupDelayAugment;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class CompiledDropperModule extends CompiledModule {
    private final int pickupDelay;  // ticks

    public CompiledDropperModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        pickupDelay = getAugmentCount(ModItems.PICKUP_DELAY_AUGMENT.get()) * PickupDelayAugment.TICKS_PER_AUGMENT;
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();
        if (getFilter().test(stack) && isRegulationOK(router, false)) {
            int nItems = Math.min(getItemsPerTick(router), stack.getCount() - getRegulationAmount());
            if (nItems <= 0) {
                return false;
            }
            ItemStack toDrop = router.peekBuffer(nItems);
            BlockPos pos = getTarget().gPos.getPos();
            Direction face = getTarget().face;
            ItemEntity item = new ItemEntity(router.getWorld(),
                    pos.getX() + 0.5 + 0.2 * face.getXOffset(),
                    pos.getY() + 0.5 + 0.2 * face.getYOffset(),
                    pos.getZ() + 0.5 + 0.2 * face.getZOffset(),
                    toDrop);
            item.setPickupDelay(pickupDelay);
            setupItemVelocity(router, item);
            router.getWorld().addEntity(item);
            router.extractBuffer(toDrop.getCount());
            return true;
        } else {
            return false;
        }
    }

    void setupItemVelocity(TileEntityItemRouter router, ItemEntity item) {
        item.setMotion(0, 0, 0);
    }
}
