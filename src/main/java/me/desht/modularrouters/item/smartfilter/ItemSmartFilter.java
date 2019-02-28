package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.filter.FilterGuiFactory;
import me.desht.modularrouters.container.BaseContainerProvider;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

@Mod.EventBusSubscriber
public abstract class ItemSmartFilter extends ItemBase {
    public ItemSmartFilter(Properties props) {
        super(props);
    }

    public abstract IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack);

    public abstract Class<? extends GuiScreen> getGuiClass();

    /**
     * Handle a filter settings message received from a client-side GUI by updating the filter itemstack appropriately.
     *
     * @param player player sending/receiving the message
     * @param message received message
     * @param filterStack item stack of the filter that needs to be updated
     * @param moduleStack item stack of the module the filter is installed in, if any (may be null)
     * @return true if a GuiSync message should be returned to the client
     */
    public abstract GuiSyncMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack);

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

    public ContainerSmartFilter createContainer(EntityPlayer player, EnumHand hand, TileEntityItemRouter router) {
        return null;
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {

    }

//    @SubscribeEvent
//    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
//        if (event.getSide() == Side.CLIENT) {
//            SubItemHandler filter = ItemSmartFilter.getFilter(event.getItemStack());
//            if (filter == null) {
//                return;
//            }
//            if (InventoryUtils.getInventory(event.getWorld(), event.getPos(), event.getFace()) != null) {
//                return;
//            }
//            // We're right-clicking an ordinary block; canceling this prevents the onArmSwing() method
//            // being called, and allows the GUI to be opened normally.
//            event.setCanceled(true);
//        }
//    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ItemSmartFilter filter = (ItemSmartFilter) stack.getItem();
        if (!world.isRemote && filter.hasContainer()) {
            NetworkHooks.openGui((EntityPlayerMP) player, new ContainerProvider(hand),
                    buf -> buf.writeBoolean(hand == EnumHand.MAIN_HAND));
        } else if (world.isRemote && !hasContainer()) {
            Minecraft.getInstance().displayGuiScreen(FilterGuiFactory.createGui(player, hand));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static class ContainerProvider extends BaseContainerProvider {
        private final EnumHand hand;
        private final String guiId;
        private final BlockPos routerPos;

        public ContainerProvider(EnumHand hand) {
            this.hand = hand;
            this.routerPos = null;
            this.guiId = "filter_held";
        }

        public ContainerProvider(BlockPos routerPos) {
            this.hand = null;
            this.routerPos = routerPos;
            this.guiId = "filter_installed";
        }

        @Override
        public Container createContainer(InventoryPlayer inventoryPlayer, EntityPlayer entityPlayer) {
            if (hand != null) {
                ItemStack stack = entityPlayer.getHeldItem(hand);
                if (stack.getItem() instanceof ItemSmartFilter) {
                    return ((ItemSmartFilter) stack.getItem()).createContainer(entityPlayer, hand, null);
                } else {
                    return null;
                }
            } else if (routerPos != null) {
                TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(entityPlayer.getEntityWorld(), routerPos);
                ItemStack filterStack = SlotTracker.getInstance(entityPlayer).getConfiguringFilter(router);
                return ((ItemSmartFilter) filterStack.getItem()).createContainer(entityPlayer, hand, router);
            } else {
                return null;
            }
        }

        @Override
        public String getGuiID() {
            return ModularRouters.MODID + ":" + guiId;
        }
    }
}
