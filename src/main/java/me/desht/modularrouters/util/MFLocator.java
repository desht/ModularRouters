package me.desht.modularrouters.util;

import com.mojang.datafixers.util.Either;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity.RecompileFlag;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;

import java.util.Optional;

/**
 * Unified object to locate a module or filter.
 */
public record MFLocator(Either<InteractionHand, RouterSlot> either, int filterSlot, ItemType itemType) {
    public static final StreamCodec<FriendlyByteBuf, MFLocator> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.either(NeoForgeStreamCodecs.enumCodec(InteractionHand.class), RouterSlot.STREAM_CODEC), MFLocator::either,
            ByteBufCodecs.VAR_INT, MFLocator::filterSlot,
            NeoForgeStreamCodecs.enumCodec(ItemType.class), MFLocator::itemType,
            MFLocator::new
    );

    public static MFLocator fromNetwork(FriendlyByteBuf buf) {
        return STREAM_CODEC.decode(buf);
    }

    private static MFLocator create(ItemType itemType, Either<InteractionHand, RouterSlot> either, int filterSlot) {
        return new MFLocator(either, filterSlot, itemType);
    }

    public static MFLocator heldModule(InteractionHand hand) {
        return create(ItemType.MODULE, Either.left(hand), -1);
    }

    public static MFLocator heldFilter(InteractionHand hand) {
        return create(ItemType.FILTER, Either.left(hand), -1);
    }

    public static MFLocator moduleInRouter(BlockPos routerPos, int routerSlot) {
        return create(ItemType.MODULE, Either.right(new RouterSlot(routerPos, routerSlot)), -1);
    }

    public static MFLocator filterInHeldModule(InteractionHand hand, int filterSlot) {
        return create(ItemType.FILTER, Either.left(hand), filterSlot);
    }

    public static MFLocator filterInInstalledModule(BlockPos routerPos, int routerSlot, int filterSlot) {
        return create(ItemType.FILTER, Either.right(new RouterSlot(routerPos, routerSlot)), filterSlot);
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        STREAM_CODEC.encode(buf, this);
    }

    public ItemStack getTargetItem(Player player) {
        if (itemType == ItemType.MODULE) {
            return either.map(
                    hand -> player.getItemInHand(hand).getItem() instanceof ModuleItem ? player.getItemInHand(hand) : ItemStack.EMPTY,
                    routerSlot -> getInstalledModule(player.level(), routerSlot)
            );
        } else if (itemType == ItemType.FILTER) {
            return either.map(
                    hand -> getFilterForStack(player.getItemInHand(hand)),
                    routerSlot -> getFilterForStack(getInstalledModule(player.level(), routerSlot))
            );
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getModuleStack(Player player) {
        return either.map(
                hand -> player.getItemInHand(hand).getItem() instanceof ModuleItem ? player.getItemInHand(hand) : ItemStack.EMPTY,
                routerSlot -> getInstalledModule(player.level(), routerSlot)
        );
    }

    public void setModuleStack(Player player, ItemStack newStack) {
        if (newStack.getItem() instanceof ModuleItem) {
            either.ifLeft(hand -> player.setItemInHand(hand, newStack))
                    .ifRight(routerSlot -> setInstalledModule(player.level(), newStack, routerSlot));
        }
    }

    public Optional<ModularRouterBlockEntity> getRouter(Level level) {
        return either.right().flatMap(routerSlot -> level.getBlockEntity(routerSlot.pos, ModBlockEntities.MODULAR_ROUTER.get()));
    }

    public Optional<InteractionHand> hand() {
        return either.left();
    }

    public Optional<RouterSlot> routerAndSlot() {
        return either.right();
    }

    public void setFilterStack(Player player, ItemStack newFilterStack) {
        if (newFilterStack.getItem() instanceof SmartFilterItem) {
            either.ifLeft(hand -> {
                        ItemStack heldStack = player.getItemInHand(hand);
                        if (heldStack.getItem() instanceof SmartFilterItem) {
                            // just replace filter in player's hand
                            player.setItemInHand(hand, newFilterStack);
                        } else if (heldStack.getItem() instanceof ModuleItem && filterSlot >= 0) {
                            // update filter in module in player's hand
                            setFilterInModule(heldStack, newFilterStack);
                        }
                    })
                    .ifRight(routerSlot -> {
                        getRouter(player.level()).ifPresent(router -> {
                            // update filter in module installed in router
                            ResourceHandler<ItemResource> modules = router.getModules();
                            ItemStack moduleStack = ItemUtil.getStack(modules, routerSlot.slot);
                            setFilterInModule(moduleStack, newFilterStack);
                            router.setChanged();
                        });
                    });
        }
    }

    private ItemStack getInstalledModule(Level level, RouterSlot routerSlot) {
        return getRouter(level)
                .map(router -> ItemUtil.getStack(router.getModules(), routerSlot.slot))
                .orElse(ItemStack.EMPTY);
    }

    private void setInstalledModule(Level level, ItemStack newStack, RouterSlot routerSlot) {
        getRouter(level).ifPresent(router -> {
            ItemStacksResourceHandler modules = router.getModules();
            modules.set(routerSlot.slot, ItemResource.of(newStack), newStack.getCount());
            router.recompileNeeded(RecompileFlag.MODULES);
            router.setChanged();
        });
    }

    private ItemStack getFilterForStack(ItemStack stack) {
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

    public enum ItemType {
        MODULE,
        FILTER
    }

    public record RouterSlot(BlockPos pos, int slot) {
        public static final StreamCodec<FriendlyByteBuf, RouterSlot> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, RouterSlot::pos,
                ByteBufCodecs.VAR_INT, RouterSlot::slot,
                RouterSlot::new
        );
    }
}
