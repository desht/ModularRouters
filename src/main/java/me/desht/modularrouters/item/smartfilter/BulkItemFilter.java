package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.container.ContainerBulkItemFilter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.handler.BaseModuleHandler.BulkFilterHandler;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.Filter.Flags;
import me.desht.modularrouters.logic.filter.matchers.BulkItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class BulkItemFilter extends SmartFilter {
    public static final int FILTER_SIZE = 54;
    private static final String NBT_ITEMS_DEPRECATED = "Items";
    private static final Flags DEF_FLAGS = new Flags((byte) 0x00);

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack, ModuleTarget target) {
        Flags flags = moduleStack.isEmpty() ? DEF_FLAGS : new Flags(moduleStack);
        SetofItemStack stacks = getFilterItems(filterStack, flags);
        return new BulkItemMatcher(stacks, flags);
    }

    private static SetofItemStack getFilterItems(ItemStack filterStack, Flags flags) {
        if (filterStack.hasTagCompound()) {
            checkAndMigrateOldNBT(filterStack);
            BulkFilterHandler handler = new BulkFilterHandler(filterStack);
            return SetofItemStack.fromItemHandler(handler, flags);
        } else {
            return new SetofItemStack(DEF_FLAGS);
        }
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        list.add(I18n.format("itemText.misc.bulkItemFilter.count", getSize(itemstack)));
    }

    @Override
    public Class<? extends GuiScreen> getGuiHandler() {
        return GuiBulkItemFilter.class;
    }

    @Override
    public boolean hasGuiContainer() {
        return true;
    }

    @Override
    public Container createContainer(EntityPlayer player, ItemStack filterStack, EnumHand hand, TileEntityItemRouter router) {
        return new ContainerBulkItemFilter(player, filterStack, hand, router);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float x, float y, float z) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (player.isSneaking()) {
            IItemHandler handler = InventoryUtils.getInventory(world, pos, face);
            if (handler != null) {
                int nAdded = mergeInventory(stack, handler);
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.inventoryMerged", nAdded, stack.getDisplayName()), false);
                world.playSound(null, pos, RegistrarMR.SOUND_SUCCESS, SoundCategory.MASTER, 1.0f, 1.0f);
                return EnumActionResult.SUCCESS;
            } else {
                return super.onItemUse(stack, player, world, pos, hand, face, x, y, z);
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    @Override
    public IMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
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
                ModularRouters.logger.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
                break;
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        if (filterStack.hasTagCompound()) {
            NBTTagCompound compound = filterStack.getTagCompound();
            if (compound.hasKey(NBT_ITEMS_DEPRECATED)) {
                // v1.1.x and earlier
                return compound.getTagList(NBT_ITEMS_DEPRECATED, Constants.NBT.TAG_COMPOUND).tagCount();
            } else {
                // v1.2.0 and later
                return BaseModuleHandler.getFilterSize(filterStack, ModuleHelper.NBT_FILTER);
            }
        } else {
            return 0;
        }
    }

    private int mergeInventory(ItemStack filterStack, IItemHandler srcInventory) {
        SetofItemStack stacks = getFilterItems(filterStack, DEF_FLAGS);
        int origSize = stacks.size();

        for (int i = 0; i < srcInventory.getSlots() && stacks.size() < FILTER_SIZE; i++) {
            ItemStack stack = srcInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack stack1 = stack.copy();
                stack1.setCount(1);
                stacks.add(stack1);
            }
        }

        BulkFilterHandler handler = new BulkFilterHandler(filterStack);
        int slot = 0;
        for (ItemStack stack : stacks.sortedList()) {
            handler.setStackInSlot(slot++, stack);
        }
        handler.save();

        return stacks.size() - origSize;
    }

    /**
     * Check for old-style (pre-v1.2.0) filter NBT and migrate it to the new format.
     *
     * @param filterStack the bulk filter item to check
     */
    public static void checkAndMigrateOldNBT(ItemStack filterStack) {
        NBTTagCompound compound = filterStack.getTagCompound();
        if (compound != null && compound.hasKey(NBT_ITEMS_DEPRECATED)) {
            // migrate the old-style bulk filter
            BulkFilterHandler handler = new BulkFilterHandler(filterStack);
            NBTTagList items = compound.getTagList(NBT_ITEMS_DEPRECATED, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < items.tagCount(); i++) {
                NBTTagCompound c = (NBTTagCompound) items.get(i);
                ItemStack stack = new ItemStack(c);
                stack.setCount(1);
                handler.setStackInSlot(i, stack);
            }
            handler.save();
            compound.removeTag(NBT_ITEMS_DEPRECATED);
        }
    }
}
