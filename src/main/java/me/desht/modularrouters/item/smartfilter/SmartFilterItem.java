package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.client.gui.filter.FilterScreenFactory;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.MRBaseItem;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class SmartFilterItem extends MRBaseItem {
    public SmartFilterItem() {
        super(ModItems.defaultProps());
    }

    @Nonnull
    public abstract IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack);

    /**
     * Handle a filter settings message received from a client-side GUI by updating the filter itemstack appropriately.
     *
     * @param player player sending/receiving the message
     * @param message received message
     * @param filterStack item stack of the filter that needs to be updated
     * @param moduleStack item stack of the module the filter is installed in, if any
     * @return true a GuiSyncMessage if a response should be sent, null otherwise
     */
    @Nullable
    public abstract GuiSyncMessage onReceiveSettingsMessage(Player player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack);

    /**
     * Get the number of items in this filter, mainly for client display purposes.
     *
     * @param filterStack item stack of the filter
     * @return the number of items
     */
    public abstract int getSize(ItemStack filterStack);

    public boolean hasContainer() {
        return true;
    }

    public ContainerSmartFilter createContainer(int windowId, Inventory invPlayer, MFLocator loc) {
        return null;
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        // nothing - override in subclasses
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SmartFilterItem filter = (SmartFilterItem) stack.getItem();
        MFLocator loc = MFLocator.heldFilter(hand);
        if (!world.isClientSide && filter.hasContainer()) {
            NetworkHooks.openGui((ServerPlayer) player, new ContainerProvider(player, loc), loc::writeBuf);
        } else if (world.isClientSide && !hasContainer()) {
            FilterScreenFactory.openFilterGui(loc);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public static class ContainerProvider implements MenuProvider {
        private final MFLocator loc;
        private final ItemStack filterStack;

        public ContainerProvider(Player player, MFLocator loc) {
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
            return ((SmartFilterItem) filterStack.getItem()).createContainer(windowId, playerInventory, loc);
        }
    }
}
