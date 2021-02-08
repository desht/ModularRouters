package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.client.gui.filter.FilterGuiFactory;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemSmartFilter extends ItemBase {
    public ItemSmartFilter() {
        super(ModItems.defaultProps());
    }

    public abstract IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack);

    /**
     * Handle a filter settings message received from a client-side GUI by updating the filter itemstack appropriately.
     *
     * @param player player sending/receiving the message
     * @param message received message
     * @param filterStack item stack of the filter that needs to be updated
     * @param moduleStack item stack of the module the filter is installed in, if any
     * @return true if a GuiSync message should be returned to the client
     */
    public abstract GuiSyncMessage onReceiveSettingsMessage(PlayerEntity player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack);

    /**
     * Get the number of items in this filter, mainly for client display purposes.
     *
     * @param filterStack item stack of the filter
     * @return the number of items
     */
    public abstract int getSize(ItemStack filterStack);

    public boolean hasContainer() {
        return false;
    }

    public ContainerSmartFilter createContainer(int windowId, PlayerInventory invPlayer, MFLocator loc) {
        return null;
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
        // nothing - override in subclasses
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ItemSmartFilter filter = (ItemSmartFilter) stack.getItem();
        MFLocator loc = MFLocator.heldFilter(hand);
        if (!world.isRemote && filter.hasContainer()) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(player, loc), loc::writeBuf);
        } else if (world.isRemote && !hasContainer()) {
            FilterGuiFactory.openFilterGui(loc);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    public static class ContainerProvider implements INamedContainerProvider {
        private final MFLocator loc;
        private final ItemStack filterStack;

        public ContainerProvider(PlayerEntity player, MFLocator loc) {
            this.loc = loc;
            this.filterStack = loc.getTargetItem(player);
        }

        @Override
        public ITextComponent getDisplayName() {
            return filterStack.getDisplayName();
        }

        @Nullable
        @Override
        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return ((ItemSmartFilter) filterStack.getItem()).createContainer(windowId, playerInventory, loc);
        }
    }
}
