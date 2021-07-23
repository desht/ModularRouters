package me.desht.modularrouters.util;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Unified object to locate a module or filter.
 */
public class MFLocator {
    public enum ItemType { MODULE, FILTER }
    public final InteractionHand hand;   // hand the player is holding the module/filter
    public final BlockPos routerPos;  // router the module is installed in
    public final int routerSlot;  // router slot that the module is in
    public final int filterSlot;  // module slot that the filter is in
    private final ItemType itemType;

    private MFLocator(ItemType itemType, InteractionHand hand, BlockPos routerPos, int routerSlot, int filterSlot) {
        this.itemType = itemType;
        this.hand = hand;
        this.routerPos = routerPos;
        this.routerSlot = routerSlot;
        this.filterSlot = filterSlot;

        Validate.isTrue(hand != null || routerPos != null && routerSlot >= 0);
    }

    public static MFLocator heldModule(InteractionHand hand) {
        return new MFLocator(ItemType.MODULE, hand, null, -1, -1);
    }

    public static MFLocator heldFilter(InteractionHand hand) {
        return new MFLocator(ItemType.FILTER, hand, null, -1, -1);
    }

    public static MFLocator moduleInRouter(BlockPos routerPos, int routerSlot) {
        return new MFLocator(ItemType.MODULE, null, routerPos, routerSlot, -1);
    }

    public static MFLocator filterInHeldModule(InteractionHand hand, int filterSlot) {
        return new MFLocator(ItemType.FILTER, hand, null, -1, filterSlot);
    }

    public static MFLocator filterInInstalledModule(BlockPos routerPos, int routerSlot, int filterSlot) {
        return new MFLocator(ItemType.FILTER, null, routerPos, routerSlot, filterSlot);
    }

    public static MFLocator fromBuffer(FriendlyByteBuf buf) {
        ItemType type = buf.readEnum(ItemType.class);
        InteractionHand hand = null;
        BlockPos routerPos = null;
        int routerSlot  = -1;
        if (buf.readBoolean()) {
            routerPos = buf.readBlockPos();
            routerSlot = buf.readByte();
        } else {
            hand = buf.readEnum(InteractionHand.class);
        }
        int filterSlot = buf.readByte();
        return new MFLocator(type, hand, routerPos, routerSlot, filterSlot);
    }

    public void writeBuf(FriendlyByteBuf buf) {
        buf.writeEnum(itemType);
        buf.writeBoolean(routerPos != null);
        if (routerPos != null) {
            buf.writeBlockPos(routerPos);
            buf.writeByte(routerSlot);
        } else {
            buf.writeEnum(hand);
        }
        buf.writeByte(filterSlot);
    }

    @Nonnull
    public ItemStack getTargetItem(Player player) {
        if (itemType == ItemType.MODULE) {
            if (hand != null) {
                return player.getItemInHand(hand).getItem() instanceof ItemModule ? player.getItemInHand(hand) : ItemStack.EMPTY;
            } else if (routerPos != null && routerSlot >= 0) {
                return getInstalledModule(player.level);
            }
        } else if (itemType == ItemType.FILTER) {
            if (hand != null) {
                return getFilterForStack(player.getItemInHand(hand));
            } else if (routerPos != null && routerSlot >= 0) {
                return getFilterForStack(getInstalledModule(player.level));
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStack getModuleStack(Player player) {
        if (hand != null) {
            return player.getItemInHand(hand).getItem() instanceof ItemModule ? player.getItemInHand(hand) : ItemStack.EMPTY;
        } else if (routerPos != null) {
            return getInstalledModule(player.level);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public Optional<ModularRouterBlockEntity> getRouter(Level world) {
        return routerPos == null ? Optional.empty() : ModularRouterBlockEntity.getRouterAt(world, routerPos);
    }

    @Nonnull
    private ItemStack getInstalledModule(Level world) {
        return getRouter(world).map(router -> router.getModules().getStackInSlot(routerSlot)).orElse(ItemStack.EMPTY);
    }

    @Nonnull
    private ItemStack getFilterForStack(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemSmartFilter) {
            return stack;
        } else if (stack.getItem() instanceof ItemModule && filterSlot >= 0) {
            return new BaseModuleHandler.ModuleFilterHandler(stack, null).getStackInSlot(filterSlot);
        } else {
            return ItemStack.EMPTY;
        }
    }
}
