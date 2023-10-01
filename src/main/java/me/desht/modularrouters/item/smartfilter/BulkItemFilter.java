package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.container.BulkItemFilterMenu;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import me.desht.modularrouters.logic.filter.matchers.BulkItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Comparator;
import java.util.List;

public class BulkItemFilter extends SmartFilterItem {
    public static final int FILTER_SIZE = 54;

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        Flags flags = moduleStack.isEmpty() ? Flags.DEFAULT_FLAGS : new Flags(moduleStack);
        SetofItemStack stacks = getFilterItems(filterStack, flags);
        return new BulkItemMatcher(stacks, flags);
    }

    private static SetofItemStack getFilterItems(ItemStack filterStack, Flags flags) {
        if (filterStack.hasTag()) {
            BulkFilterHandler handler = new BulkFilterHandler(filterStack, null);
            return SetofItemStack.fromItemHandler(handler, flags);
        } else {
            return new SetofItemStack(Flags.DEFAULT_FLAGS);
        }
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<Component> list) {
        super.addExtraInformation(itemstack, list);
        list.add(ClientUtil.xlate("modularrouters.itemText.misc.bulkItemFilter.count", getSize(itemstack)));
    }

    @Override
    public AbstractSmartFilterMenu createMenu(int windowId, Inventory invPlayer, MFLocator loc) {
        return new BulkItemFilterMenu(windowId, invPlayer, loc);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player != null && player.isSteppingCarefully()) {
            return InventoryUtils.getInventory(world, ctx.getClickedPos(), ctx.getClickedFace()).map(handler -> {
                int nAdded = mergeInventory(stack, handler);
                player.displayClientMessage(Component.translatable("modularrouters.chatText.misc.inventoryMerged", nAdded, stack.getHoverName()), false);
                world.playSound(null, ctx.getClickedPos(), ModSounds.SUCCESS.get(), SoundSource.MASTER,
                        ConfigHolder.client.sound.bleepVolume.get().floatValue(), 1.0f);
                return InteractionResult.SUCCESS;
            }).orElse(super.useOn(ctx));
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public GuiSyncMessage onReceiveSettingsMessage(Player player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        if (player.containerMenu instanceof BulkItemFilterMenu con) {
            Flags flags = moduleStack.isEmpty() ? Flags.DEFAULT_FLAGS : new Flags(moduleStack);
            switch (message.getOp()) {
                case CLEAR_ALL -> con.clearSlots();
                case MERGE -> message.getTargetInventory().ifPresent(h -> con.mergeInventory(h, flags, false));
                case LOAD -> message.getTargetInventory().ifPresent(h -> con.mergeInventory(h, flags, true));
                default -> ModularRouters.LOGGER.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
            }
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return BaseModuleHandler.getFilterSize(filterStack, ModuleHelper.NBT_FILTER);
    }

    private int mergeInventory(ItemStack filterStack, IItemHandler srcInventory) {
        SetofItemStack stacks = getFilterItems(filterStack, Flags.DEFAULT_FLAGS);
        int origSize = stacks.size();

        for (int i = 0; i < srcInventory.getSlots() && stacks.size() < FILTER_SIZE; i++) {
            ItemStack stack = srcInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(ItemHandlerHelper.copyStackWithSize(stack, 1));
            }
        }

        BulkFilterHandler handler = new BulkFilterHandler(filterStack, null);
        int slot = 0;
        Comparator<ItemStack> comp = (o1, o2) -> o1.getHoverName().toString().compareTo(o2.getHoverName().getString());
        for (ItemStack stack : stacks.stream().sorted(comp).toList()) {
            handler.setStackInSlot(slot++, stack);
        }
        handler.save();

        return stacks.size() - origSize;
    }
}
