package me.desht.modularrouters.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Optional;

public class InventoryUtils {
    private static final double MOTION_SCALE = 0.05;

    /**
     * Drop all items from the given item handler into the world as item entities with random offsets & motions.
     *
     * @param world the world
     * @param pos blockpos to drop at (usually position of the item handler tile entity)
     * @param itemHandler the item handler
     */
    public static void dropInventoryItems(Level world, BlockPos pos, IItemHandler itemHandler) {
        RandomSource random = world.random;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemStack = itemHandler.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                double offsetX = random.nextDouble() * 0.8 + 0.1;
                double offsetY = random.nextDouble() * 0.8 + 0.1;
                double offsetZ = random.nextDouble() * 0.8 + 0.1;
                ItemEntity entityitem = new ItemEntity(world, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, itemStack.copy());
                entityitem.setDeltaMovement(random.nextGaussian() * MOTION_SCALE, random.nextGaussian() * MOTION_SCALE + 0.2, random.nextGaussian() * 0.05);
                world.addFreshEntity(entityitem);
            }
        }
    }

    public static Optional<IItemHandler> getInventory(Level world, BlockPos pos, @Nullable Direction side) {
        return Optional.ofNullable(world.getCapability(Capabilities.Item.BLOCK, pos, side));
    }

    /**
     * Transfer some items from the given slot in the given source item handler to the given destination handler.
     *
     * @param from source item handler
     * @param to destination item handler
     * @param slot slot in the source handler
     * @param count number of items to attempt to transfer
     * @return number of items actually transferred
     */
    public static int transferItems(IItemHandler from, IItemHandler to, int slot, int count) {
        if (from == null || to == null || count == 0) {
            return 0;
        }
        ItemStack toSend = from.extractItem(slot, count, true);
        if (toSend.isEmpty()) {
            return 0;
        }
        ItemStack excess = ItemHandlerHelper.insertItem(to, toSend, false);
        int inserted = toSend.getCount() - excess.getCount();
        from.extractItem(slot, inserted, false);
        return inserted;
    }

    /**
     * Drop an item stack into the world as an item entity.
     *
     * @param world the world
     * @param pos the block position (entity will spawn in centre of block pos)
     * @param stack itemstack to drop
     * @return true if the entity was spawned, false otherwise
     */
    public static boolean dropItems(Level world, Vec3 pos, ItemStack stack) {
        if (!world.isClientSide()) {
            ItemEntity item = new ItemEntity(world, pos.x(), pos.y(), pos.z(), stack);
            return world.addFreshEntity(item);
        }
        return true;
    }

    /**
     * Get a count of the given item in the given item handler.  NBT data is not considered.
     *
     * @param toCount the item to count
     * @param handler the inventory to check
     * @param max maximum number of items to count
     * @param matchMeta whether to consider item metadata
     * @return number of items found, or the supplied max, whichever is smaller
     */
    public static int countItems(ItemStack toCount, IItemHandler handler, int max, boolean matchMeta) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                boolean match;
                if (matchMeta) {
                    match = stack.is(toCount.getItem()) && (!stack.isDamageableItem() || stack.getDamageValue() == toCount.getDamageValue());
                } else {
                    match = stack.is(toCount.getItem());
                }
                if (match) {
                    count += stack.getCount();
                }
                if (count >= max) return max;
            }
        }
        return count;
    }
}
