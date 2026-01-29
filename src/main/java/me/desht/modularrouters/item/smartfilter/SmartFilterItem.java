package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.client.gui.filter.FilterScreenFactory;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.item.MRBaseItem;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class SmartFilterItem extends MRBaseItem {
//    public SmartFilterItem() {
//        super(ModItems.defaultProps());
//    }

    public SmartFilterItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    public abstract IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack);

    /**
     * Get the number of items in this filter, mainly for client display purposes.
     *
     * @param filterStack item stack of the filter
     * @return the number of items
     */
    public abstract int getSize(ItemStack filterStack);

    public boolean hasMenu() {
        return true;
    }

    public AbstractSmartFilterMenu createMenu(int windowId, Inventory invPlayer, MFLocator loc) {
        return null;
    }

    @Override
    protected void addExtraInformation(ItemStack stack, Consumer<Component> tooltipAdder) {
        // nothing - override in subclasses
    }

    protected final void addCountInfo(Consumer<Component> consumer, int count) {
        consumer.accept(ClientUtil.xlate("modularrouters.itemText.misc.filter.count", count).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SmartFilterItem filter = (SmartFilterItem) stack.getItem();
        MFLocator loc = MFLocator.heldFilter(hand);
        if (player instanceof ServerPlayer sp && filter.hasMenu()) {
            sp.openMenu(new FilterMenuProvider(player, loc), loc::toNetwork);
        } else if (world.isClientSide() && !hasMenu()) {
            FilterScreenFactory.openFilterGui(loc);
        }
        return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    public static class FilterMenuProvider implements MenuProvider {
        private final MFLocator loc;
        private final ItemStack filterStack;

        public FilterMenuProvider(Player player, MFLocator loc) {
            this.loc = loc;
            this.filterStack = loc.getTargetItem(player);
        }

        @Override
        public Component getDisplayName() {
            return filterStack.getHoverName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
            return ((SmartFilterItem) filterStack.getItem()).createMenu(windowId, playerInventory, loc);
        }
    }
}
