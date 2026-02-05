package me.desht.modularrouters.util;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity.RecompileFlag;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Unified object to locate a module or filter.
 */
public record MFLocator(InteractionHand hand, BlockPos routerPos, int routerSlot, int filterSlot, ItemType itemType) {
    public enum ItemType { MODULE, FILTER;}

    public static final StreamCodec<FriendlyByteBuf,MFLocator> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public MFLocator decode(FriendlyByteBuf buf) {
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
            return create(type, hand, routerPos, routerSlot, filterSlot);
        }

        @Override
        public void encode(FriendlyByteBuf buf, MFLocator locator) {
            buf.writeEnum(locator.itemType);
            buf.writeBoolean(locator.routerPos != null);
            if (locator.routerPos != null) {
                buf.writeBlockPos(locator.routerPos);
                buf.writeByte(locator.routerSlot);
            } else {
                buf.writeEnum(locator.hand);
            }
            buf.writeByte(locator.filterSlot);
        }
    };

    public static MFLocator fromNetwork(FriendlyByteBuf buf) {
        return STREAM_CODEC.decode(buf);
    }

    private static MFLocator create(ItemType itemType, InteractionHand hand, BlockPos routerPos, int routerSlot, int filterSlot) {
        Validate.isTrue(hand != null || routerPos != null && routerSlot >= 0);

        return new MFLocator(hand, routerPos, routerSlot, filterSlot, itemType);
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        STREAM_CODEC.encode(buf, this);
    }

    public static MFLocator heldModule(InteractionHand hand) {
        return create(ItemType.MODULE, hand, null, -1, -1);
    }

    public static MFLocator heldFilter(InteractionHand hand) {
        return create(ItemType.FILTER, hand, null, -1, -1);
    }

    public static MFLocator moduleInRouter(BlockPos routerPos, int routerSlot) {
        return create(ItemType.MODULE, null, routerPos, routerSlot, -1);
    }

    public static MFLocator filterInHeldModule(InteractionHand hand, int filterSlot) {
        return create(ItemType.FILTER, hand, null, -1, filterSlot);
    }

    public static MFLocator filterInInstalledModule(BlockPos routerPos, int routerSlot, int filterSlot) {
        return create(ItemType.FILTER, null, routerPos, routerSlot, filterSlot);
    }

    @Nonnull
    public ItemStack getTargetItem(Player player) {
        if (itemType == ItemType.MODULE) {
            if (hand != null) {
                return player.getItemInHand(hand).getItem() instanceof ModuleItem ? player.getItemInHand(hand) : ItemStack.EMPTY;
            } else if (routerPos != null && routerSlot >= 0) {
                return getInstalledModule(player.level());
            }
        } else if (itemType == ItemType.FILTER) {
            if (hand != null) {
                return getFilterForStack(player.getItemInHand(hand));
            } else if (routerPos != null && routerSlot >= 0) {
                return getFilterForStack(getInstalledModule(player.level()));
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStack getModuleStack(Player player) {
        if (hand != null) {
            return player.getItemInHand(hand).getItem() instanceof ModuleItem ? player.getItemInHand(hand) : ItemStack.EMPTY;
        } else if (routerPos != null) {
            return getInstalledModule(player.level());
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void setModuleStack(Player player, ItemStack newStack) {
        if (newStack.getItem() instanceof ModuleItem) {
            if (hand != null) {
                player.setItemInHand(hand, newStack);
            } else if (routerPos != null) {
                setInstalledModule(player.level(), newStack);
            }
        }
    }

    public Optional<ModularRouterBlockEntity> getRouter(Level level) {
        return routerPos == null ? Optional.empty() : level.getBlockEntity(routerPos, ModBlockEntities.MODULAR_ROUTER.get());
    }

    public void setFilterStack(Player player, ItemStack newFilterStack) {
        if (newFilterStack.getItem() instanceof SmartFilterItem) {
            if (routerPos == null) {
                ItemStack heldStack = player.getItemInHand(hand);
                if (heldStack.getItem() instanceof SmartFilterItem) {
                    // just replace filter in player's hand
                    player.setItemInHand(hand, newFilterStack);
                } else if (heldStack.getItem() instanceof ModuleItem && filterSlot >= 0) {
                    // update filter in module in player's hand
                    setFilterInModule(heldStack, newFilterStack);
                }
            } else {
                getRouter(player.level()).ifPresent(router -> {
                    // update filter in module installed in router
                    ResourceHandler<ItemResource> modules = router.getModules();
                    ItemStack moduleStack = ItemUtil.getStack(modules, routerSlot);
                    setFilterInModule(moduleStack, newFilterStack);
                    router.setChanged();
                });
            }
        }
    }

    @Nonnull
    private ItemStack getInstalledModule(Level level) {
        return getRouter(level)
                .map(router -> ItemUtil.getStack((ResourceHandler<ItemResource>) router.getModules(), routerSlot))
                .orElse(ItemStack.EMPTY);
    }

    private void setInstalledModule(Level level, ItemStack newStack) {
        getRouter(level).ifPresent(router -> {
            ItemStacksResourceHandler modules = (ItemStacksResourceHandler) router.getModules();
            modules.set(routerSlot, ItemResource.of(newStack), newStack.getCount());
            router.recompileNeeded(RecompileFlag.MODULES);
            router.setChanged();
        });
    }

    @Nonnull
    private ItemStack getFilterForStack(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof SmartFilterItem) {
            return stack;
        } else if (stack.getItem() instanceof ModuleItem && filterSlot >= 0) {
            return new ModuleFilterHandler(stack, null).getStackInSlot(filterSlot);
        } else {
            return ItemStack.EMPTY;
        }
    }

    private void setFilterInModule(ItemStack moduleStack, ItemStack filterStack) {
        ModuleFilterHandler handler = new ModuleFilterHandler(moduleStack, null);
        handler.setStackInSlot(filterSlot, filterStack);
        handler.save();
    }
}
