package me.desht.modularrouters.util;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class InventoryUtils {
    /**
     * Drop all items from the given item handler into the world as item entities with random offsets & motions.
     *
     * @param world the world
     * @param pos blockpos to drop at (usually position of the item handler tile entity)
     * @param itemHandler the item handler
     */
    public static void dropInventoryItems(World world, BlockPos pos, IItemHandler itemHandler) {
        Random random = new Random();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemStack = itemHandler.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                float offsetX = random.nextFloat() * 0.8f + 0.1f;
                float offsetY = random.nextFloat() * 0.8f + 0.1f;
                float offsetZ = random.nextFloat() * 0.8f + 0.1f;
                while (!itemStack.isEmpty()) {
                    int stackSize = Math.min(itemStack.getCount(), random.nextInt(21) + 10);
                    ItemEntity entityitem = new ItemEntity(world, pos.getX() + (double) offsetX, pos.getY() + (double) offsetY, pos.getZ() + (double) offsetZ, new ItemStack(itemStack.getItem(), stackSize));
                    if (itemStack.hasTag()) {
                        entityitem.getItem().setTag(itemStack.getTag().copy());
                    }
                    itemStack.shrink(stackSize);

                    float motionScale = 0.05f;
                    entityitem.setMotion(random.nextGaussian() * (double) motionScale, random.nextGaussian() * (double) motionScale + 0.2, random.nextGaussian() * (double) motionScale);
                    world.addEntity(entityitem);
                }
            }
        }
    }

    public static LazyOptional<IItemHandler> getInventory(World world, BlockPos pos, @Nullable Direction side) {
        TileEntity te = world.getTileEntity(pos);
        return te == null ? LazyOptional.empty() : te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
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
    public static boolean dropItems(World world, Vector3d pos, ItemStack stack) {
        if (!world.isRemote) {
            ItemEntity item = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            return world.addEntity(item);
        }
        return true;
    }

    /**
     * Get a count of the given item in the given item handler.  NBT data is not considered.
     *
     * @param toCount the item to count
     * @param handler the inventory to check
     * @param max maximum number of items to count
     * @param matchMeta whether or not to consider item metadata
     * @return number of items found, or the supplied max, whichever is smaller
     */
    public static int countItems(ItemStack toCount, IItemHandler handler, int max, boolean matchMeta) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                boolean match = matchMeta ? stack.isItemEqual(toCount) : stack.isItemEqualIgnoreDurability(toCount);
                if (match) {
                    count += stack.getCount();
                }
                if (count >= max) return max;
            }
        }
        return count;
    }
}
