package me.desht.modularrouters.network;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.BulkItemFilterMenu;
import me.desht.modularrouters.container.FilterSlot;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.SyncUpgrade;
import me.desht.modularrouters.network.messages.*;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Objects;

public class ServerPayloadHandler {
    public static void handleData(FilterSettingsMessage message, PlayPayloadContext context) {
        context.player().ifPresent(player ->  context.workHandler().submitAsync(() -> {
            MFLocator locator = message.locator();
            ItemStack moduleStack = locator.getModuleStack(player);
            ItemStack filterStack = locator.getTargetItem(player);
            if (filterStack.getItem() instanceof SmartFilterItem sf) {
                context.workHandler().submitAsync(() -> {
                    GuiSyncMessage response = sf.onReceiveSettingsMessage(player, message, filterStack, moduleStack);
                    if (!moduleStack.isEmpty()) {
                        ModularRouterBlockEntity router = locator.getRouter(player.level()).orElse(null);
                        BaseModuleHandler.ModuleFilterHandler filterHandler = new BaseModuleHandler.ModuleFilterHandler(moduleStack, router);
                        filterHandler.setStackInSlot(locator.filterSlot(), filterStack);
                        filterHandler.save();
                        if (locator.hand() != null) {
                            player.setItemInHand(locator.hand(), filterHandler.getHolderStack());
                        } else if (router != null) {
                            router.recompileNeeded(ModularRouterBlockEntity.COMPILE_MODULES);
                        }
                    }
                    if (response != null) {
                        // send to any nearby players in case they also have the GUI open
                        PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(),
                                64, player.getCommandSenderWorld().dimension());
                        PacketDistributor.NEAR.with(tp).send(response);
                    }

                });
            }
        }));
    }

    public static void handleData(ModuleFilterMessage message, PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            AbstractContainerMenu c = player.containerMenu;
            int slot = message.slot();
            if (isValidContainer(c) && slot >= 0 && slot < c.slots.size() && c.getSlot(slot) instanceof FilterSlot) {
                c.getSlot(slot).set(message.stack());
            }
        }));
    }

    private static boolean isValidContainer(AbstractContainerMenu c) {
        return c instanceof ModuleMenu || c instanceof BulkItemFilterMenu;
    }

    public static void handleData(ModuleSettingsMessage message, PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            MFLocator locator = message.locator();
            CompoundTag payload = message.payload();
            ItemStack moduleStack = locator.getModuleStack(player);

            if (moduleStack.getItem() instanceof ModuleItem) {
                CompoundTag compound = ModuleHelper.validateNBTForWriting(moduleStack);
                for (String key : payload.getAllKeys()) {
                    compound.put(key, Objects.requireNonNull(payload.get(key)));
                }
                if (locator.routerPos() != null) {
                    player.getCommandSenderWorld().getBlockEntity(locator.routerPos(), ModBlockEntities.MODULAR_ROUTER.get())
                            .ifPresent(router -> router.recompileNeeded(ModularRouterBlockEntity.COMPILE_MODULES));
                }
            } else {
                ModularRouters.LOGGER.warn("ignoring ModuleSettingsMessage for " + player.getGameProfile().getName() + " - expected module not found @ " + locator);
            }
        }));
    }

    public static void handleData(OpenGuiMessage message, PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            MFLocator locator = message.locator();
            switch (message.op()) {
                case ROUTER ->
                    // item router GUI
                        locator.getRouter(player.getCommandSenderWorld())
                                .ifPresent(router -> player.openMenu(router, locator.routerPos()));
                case MODULE_HELD ->
                    // module held in player's hand
                        player.openMenu(new ModuleItem.ModuleMenuProvider(player, locator), locator::writeBuf);
                case MODULE_INSTALLED ->
                    // module installed in a router
                        locator.getRouter(player.getCommandSenderWorld())
                                .ifPresent(router -> player.openMenu(new ModuleItem.ModuleMenuProvider(player, locator), locator::writeBuf));
                case FILTER_HELD ->
                    // filter is in a module in player's hand
                        player.openMenu(new SmartFilterItem.FilterMenuProvider(player, locator), locator::writeBuf);
                case FILTER_INSTALLED ->
                    // filter is in a module in a router
                        locator.getRouter(player.getCommandSenderWorld())
                                .ifPresent(router -> player.openMenu(new SmartFilterItem.FilterMenuProvider(player, locator), locator::writeBuf));
            }
        }));
    }

    public static void handleData(SyncUpgradeSettingsMessage message, PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            ItemStack held = player.getItemInHand(message.hand());
            if (held.getItem() instanceof SyncUpgrade) {
                SyncUpgrade.setTunedValue(held, message.tunedValue());
            }
        }));
    }

    public static void handleData(ValidateModuleMessage message, PlayPayloadContext context) {
        context.player().ifPresent(player -> context.workHandler().submitAsync(() -> {
            ItemStack stack = player.getItemInHand(message.hand());
            if (stack.getItem() instanceof ModuleItem moduleItem && player instanceof ServerPlayer sp) {
                moduleItem.doModuleValidation(stack, sp);
            }
        }));
    }
}
