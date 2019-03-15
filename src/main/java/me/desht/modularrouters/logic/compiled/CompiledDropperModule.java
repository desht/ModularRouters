package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.item.augment.PickupDelayAugment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class CompiledDropperModule extends CompiledModule {
    private final int pickupDelay;  // ticks

    public CompiledDropperModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        pickupDelay = getAugmentCount(ObjectRegistry.PICKUP_DELAY_AUGMENT) * PickupDelayAugment.TICKS_PER_AUGMENT;
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
            BlockPos pos = getTarget().pos;
            EnumFacing face = getTarget().face;
            EntityItem item = new EntityItem(router.getWorld(),
                    pos.getX() + 0.5 + 0.2 * face.getXOffset(),
                    pos.getY() + 0.5 + 0.2 * face.getYOffset(),
                    pos.getZ() + 0.5 + 0.2 * face.getZOffset(),
                    toDrop);
            item.setPickupDelay(pickupDelay);
            setupItemVelocity(router, item);
            router.getWorld().spawnEntity(item);
            router.extractBuffer(toDrop.getCount());
            return true;
        } else {
            return false;
        }
    }

    void setupItemVelocity(TileEntityItemRouter router, EntityItem item) {
        item.motionX = item.motionY = item.motionZ = 0.0;
    }
}
