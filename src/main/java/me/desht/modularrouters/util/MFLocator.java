package me.desht.modularrouters.util;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

/**
 * Unified object to locate a module or filter.
 */
public class MFLocator {
    public enum ItemType { MODULE, FILTER }
    public final Hand hand;   // hand the player is holding the module/filter
    public final BlockPos routerPos;  // router the module is installed in
    public final int routerSlot;  // router slot that the module is in
    public final int filterSlot;  // module slot that the filter is in
    private final ItemType itemType;

    private MFLocator(ItemType itemType, Hand hand, BlockPos routerPos, int routerSlot, int filterSlot) {
        this.itemType = itemType;
        this.hand = hand;
        this.routerPos = routerPos;
        this.routerSlot = routerSlot;
        this.filterSlot = filterSlot;

        Validate.isTrue(hand != null || routerPos != null && routerSlot >= 0);
    }

    public static MFLocator heldModule(Hand hand) {
        return new MFLocator(ItemType.MODULE, hand, null, -1, -1);
    }

    public static MFLocator heldFilter(Hand hand) {
        return new MFLocator(ItemType.FILTER, hand, null, -1, -1);
    }

    public static MFLocator moduleInRouter(BlockPos routerPos, int routerSlot) {
        return new MFLocator(ItemType.MODULE, null, routerPos, routerSlot, -1);
    }

    public static MFLocator filterInHeldModule(Hand hand, int filterSlot) {
        return new MFLocator(ItemType.FILTER, hand, null, -1, filterSlot);
    }

    public static MFLocator filterInInstalledModule(BlockPos routerPos, int routerSlot, int filterSlot) {
        return new MFLocator(ItemType.FILTER, null, routerPos, routerSlot, filterSlot);
    }

    public static MFLocator fromBuffer(PacketBuffer buf) {
        ItemType type = ItemType.values()[buf.readByte()];
        Hand hand = null;
        BlockPos routerPos = null;
        int routerSlot  = -1;
        if (buf.readBoolean()) {
            routerPos = buf.readBlockPos();
            routerSlot = buf.readByte();
        } else {
            hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        }
        int filterSlot = buf.readByte();
        return new MFLocator(type, hand, routerPos, routerSlot, filterSlot);
    }

    public void writeBuf(PacketBuffer buf) {
        buf.writeByte(itemType.ordinal());
        buf.writeBoolean(routerPos != null);
        if (routerPos != null) {
            buf.writeBlockPos(routerPos);
            buf.writeByte(routerSlot);
        } else {
            buf.writeBoolean(hand == Hand.MAIN_HAND);
        }
        buf.writeByte(filterSlot);
    }

    @Nonnull
    public ItemStack getTargetItem(PlayerEntity player) {
        if (itemType == ItemType.MODULE) {
            if (hand != null) {
                return player.getHeldItem(hand).getItem() instanceof ItemModule ? player.getHeldItem(hand) : ItemStack.EMPTY;
            } else if (routerPos != null && routerSlot >= 0) {
                return getInstalledModule(player.world);
            }
        } else if (itemType == ItemType.FILTER) {
            if (hand != null) {
                return getFilterForStack(player.getHeldItem(hand));
            } else if (routerPos != null && routerSlot >= 0) {
                return getFilterForStack(getInstalledModule(player.world));
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStack getModuleStack(PlayerEntity player) {
        if (hand != null) {
            return player.getHeldItem(hand).getItem() instanceof ItemModule ? player.getHeldItem(hand) : ItemStack.EMPTY;
        } else if (routerPos != null) {
            return getInstalledModule(player.world);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public TileEntityItemRouter getRouter(World world) {
        return routerPos == null ? null : TileEntityItemRouter.getRouterAt(world, routerPos);
    }

    @Nonnull
    private ItemStack getInstalledModule(World world) {
        TileEntityItemRouter router = getRouter(world);
        return router == null ? ItemStack.EMPTY : router.getModules().getStackInSlot(routerSlot);
    }

    @Nonnull
    private ItemStack getFilterForStack(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemSmartFilter) {
            return stack;
        } else if (stack.getItem() instanceof ItemModule && filterSlot >= 0) {
            return new BaseModuleHandler.ModuleFilterHandler(stack).getStackInSlot(filterSlot);
        } else {
            return ItemStack.EMPTY;
        }
    }
}
