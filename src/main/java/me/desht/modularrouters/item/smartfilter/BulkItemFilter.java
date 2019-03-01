package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.container.ContainerBulkItemFilter;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import me.desht.modularrouters.logic.filter.matchers.BulkItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.HashableItemStackWrapper;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BulkItemFilter extends ItemSmartFilter {
    public static final int FILTER_SIZE = 54;
    private static final Flags DEF_FLAGS = new Flags((byte) 0x00);

    public BulkItemFilter(Properties props) {
        super(props);
    }

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        Flags flags = moduleStack.isEmpty() ? DEF_FLAGS : new Flags(moduleStack);
        Set<HashableItemStackWrapper> stacks = getFilterItems(filterStack, flags);
        return new BulkItemMatcher(stacks, flags);
    }

    private static Set<HashableItemStackWrapper> getFilterItems(ItemStack filterStack, Flags flags) {
        if (filterStack.hasTag()) {
            BulkFilterHandler handler = new BulkFilterHandler(filterStack);
            return HashableItemStackWrapper.makeSet(handler, flags);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addExtraInformation(itemstack, list);
        list.add(new TextComponentTranslation("itemText.misc.bulkItemFilter.count", getSize(itemstack)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<? extends GuiScreen> getGuiClass() {
        return GuiBulkItemFilter.class;
    }

    @Override
    public boolean hasContainer() {
        return true;
    }

    @Override
    public ContainerSmartFilter createContainer(EntityPlayer player, EnumHand hand, TileEntityItemRouter router) {
        return new ContainerBulkItemFilter(player, hand, router);
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext ctx) {
        World world = ctx.getWorld();
        EntityPlayer player = ctx.getPlayer();
        ItemStack stack = ctx.getItem();
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (player.isSneaking()) {
            IItemHandler handler = InventoryUtils.getInventory(world, ctx.getPos(), ctx.getFace());
            if (handler != null) {
                int nAdded = mergeInventory(stack, handler);
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.inventoryMerged", nAdded, stack.getDisplayName()), false);
                world.playSound(null, ctx.getPos(), ObjectRegistry.SOUND_SUCCESS, SoundCategory.MASTER, 1.0f, 1.0f);
                return EnumActionResult.SUCCESS;
            } else {
                return super.onItemUse(ctx);
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    @Override
    public GuiSyncMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        ContainerBulkItemFilter con = player.openContainer instanceof ContainerBulkItemFilter ?
                (ContainerBulkItemFilter) player.openContainer : null;
        Flags flags = moduleStack.isEmpty() ? DEF_FLAGS : new Flags(moduleStack);

        switch (message.getOp()) {
            case CLEAR_ALL:
                if (con != null) con.clearSlots();
                break;
            case MERGE:
                if (con != null) con.mergeInventory(message.getTargetInventory(), flags, false);
                break;
            case LOAD:
                if (con != null) con.mergeInventory(message.getTargetInventory(), flags, true);
                break;
            default:
                ModularRouters.LOGGER.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
                break;
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        if (filterStack.hasTag()) {
            NBTTagCompound compound = filterStack.getTag();
            return BaseModuleHandler.getFilterSize(filterStack, ModuleHelper.NBT_FILTER);
        } else {
            return 0;
        }
    }

    private int mergeInventory(ItemStack filterStack, IItemHandler srcInventory) {
        Set<HashableItemStackWrapper> stacks = getFilterItems(filterStack, DEF_FLAGS);
        int origSize = stacks.size();

        for (int i = 0; i < srcInventory.getSlots() && stacks.size() < FILTER_SIZE; i++) {
            ItemStack stack = srcInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack stack1 = stack.copy();
                stack1.setCount(1);
                stacks.add(new HashableItemStackWrapper(stack1, DEF_FLAGS));
            }
        }

        BulkFilterHandler handler = new BulkFilterHandler(filterStack);
        int slot = 0;
        for (ItemStack stack : stacks.stream().sorted().map(HashableItemStackWrapper::getStack).collect(Collectors.toList())) {
            handler.setStackInSlot(slot++, stack);
        }
        handler.save();

        return stacks.size() - origSize;
    }
}
