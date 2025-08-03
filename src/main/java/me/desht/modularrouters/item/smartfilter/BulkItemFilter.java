package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.container.BulkItemFilterMenu;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.logic.filter.matchers.BulkItemMatcher;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

public class BulkItemFilter extends SmartFilterItem {
    public static final int FILTER_SIZE = 54;

    public BulkItemFilter(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        ModuleFlags flags = ModuleFlags.forItem(moduleStack);

        SetofItemStack stacks = getFilterItems(filterStack, flags);
        return new BulkItemMatcher(stacks, flags);
    }

    private static SetofItemStack getFilterItems(ItemStack filterStack, ModuleFlags flags) {
        BulkFilterHandler handler = new BulkFilterHandler(filterStack, null);
        return SetofItemStack.fromItemHandler(handler, flags);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, Consumer<Component> list) {
        super.addExtraInformation(itemstack, list);
        addCountInfo(list, getSize(itemstack));
    }

    @Override
    public AbstractSmartFilterMenu createMenu(int windowId, Inventory invPlayer, MFLocator loc) {
        return new BulkItemFilterMenu(windowId, invPlayer, loc);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        return ctx.getLevel().isClientSide ? InteractionResult.SUCCESS : handleUseServerSide(ctx);
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return BaseModuleHandler.getFilterItemCount(filterStack);
    }

    private InteractionResult handleUseServerSide(UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer sp) {
            Optional<IItemHandler> inventory = InventoryUtils.getInventory(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace());
            if (inventory.isPresent()) {
                int nAdded = mergeInventory(ctx.getItemInHand(), inventory.get());
                sp.displayClientMessage(Component.translatable("modularrouters.chatText.misc.inventoryMerged",
                        nAdded, ctx.getItemInHand().getHoverName()), false);
                ctx.getLevel().playSound(null, ctx.getClickedPos(), ModSounds.SUCCESS.get(), SoundSource.MASTER,
                        ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.0f);
            } else {
                MFLocator loc = MFLocator.heldFilter(ctx.getHand());
                sp.openMenu(new FilterMenuProvider(sp, loc), loc::toNetwork);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private int mergeInventory(ItemStack filterStack, IItemHandler srcInventory) {
        SetofItemStack stacks = getFilterItems(filterStack, ModuleFlags.DEFAULT);
        int origSize = stacks.size();

        for (int i = 0; i < srcInventory.getSlots() && stacks.size() < FILTER_SIZE; i++) {
            ItemStack stack = srcInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copyWithCount(1));
            }
        }

        BulkFilterHandler handler = new BulkFilterHandler(filterStack, null);
        int slot = 0;
        Comparator<ItemStack> comp = (s1, s2) -> s1.getHoverName().toString().compareTo(s2.getHoverName().getString());
        for (ItemStack stack : stacks.stream().sorted(comp).toList()) {
            handler.setStackInSlot(slot++, stack);
        }
        handler.save();

        return stacks.size() - origSize;
    }
}
