package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.container.ContainerBulkItemFilter;
import me.desht.modularrouters.container.ContainerSmartFilter;
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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BulkItemFilter extends ItemSmartFilter {
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
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addExtraInformation(itemstack, list);
        list.add(new TranslationTextComponent("itemText.misc.bulkItemFilter.count", getSize(itemstack)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<? extends Screen> getGuiClass() {
        return GuiBulkItemFilter.class;
    }

    @Override
    public boolean hasContainer() {
        return true;
    }

    @Override
    public ContainerSmartFilter createContainer(int windowId, PlayerInventory invPlayer, MFLocator loc) {
        return new ContainerBulkItemFilter(windowId, invPlayer, loc);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        World world = ctx.getWorld();
        PlayerEntity player = ctx.getPlayer();
        ItemStack stack = ctx.getItem();
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        } else if (player != null && player.isSteppingCarefully()) {
            return InventoryUtils.getInventory(world, ctx.getPos(), ctx.getFace()).map(handler -> {
                int nAdded = mergeInventory(stack, handler);
                player.sendStatusMessage(new TranslationTextComponent("chatText.misc.inventoryMerged", nAdded, stack.getDisplayName()), false);
                world.playSound(null, ctx.getPos(), ModSounds.SUCCESS.get(), SoundCategory.MASTER, 1.0f, 1.0f);
                return ActionResultType.SUCCESS;
            }).orElse(super.onItemUse(ctx));
        } else {
            return ActionResultType.PASS;
        }
    }

    @Override
    public GuiSyncMessage onReceiveSettingsMessage(PlayerEntity player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        if (!(player.openContainer instanceof ContainerBulkItemFilter)) {
            return null;
        }

        ContainerBulkItemFilter con = (ContainerBulkItemFilter) player.openContainer;
        Flags flags = moduleStack.isEmpty() ? Flags.DEFAULT_FLAGS : new Flags(moduleStack);
        switch (message.getOp()) {
            case CLEAR_ALL:
                con.clearSlots();
                break;
            case MERGE:
                message.getTargetInventory().ifPresent(h -> con.mergeInventory(h, flags, false));
                break;
            case LOAD:
                message.getTargetInventory().ifPresent(h -> con.mergeInventory(h, flags, true));
                break;
            default:
                ModularRouters.LOGGER.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
                break;
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
        Comparator<ItemStack> comp = (o1, o2) -> o1.getDisplayName().toString().compareTo(o2.getDisplayName().getString());
        for (ItemStack stack : stacks.stream().sorted(comp).collect(Collectors.toList())) {
            handler.setStackInSlot(slot++, stack);
        }
        handler.save();

        return stacks.size() - origSize;
    }
}
