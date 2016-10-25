package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.gui.filter.GuiBulkItemFilter;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.logic.RouterTarget;
import me.desht.modularrouters.logic.filter.matchers.BulkItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.sound.MRSoundEvents;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.SetofItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;

public class BulkItemFilter extends SmartFilter {
    private static final String NBT_ITEMS = "Items";
    private static final int MAX_SIZE = 54;

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack, RouterTarget target) {
        return new BulkItemMatcher(getFilterItems(filterStack), target);
    }

    public static SetofItemStack getFilterItems(ItemStack filterStack) {
        if (filterStack.hasTagCompound()) {
            NBTTagCompound compound = filterStack.getTagCompound();
            NBTTagList items = compound.getTagList(NBT_ITEMS, Constants.NBT.TAG_COMPOUND);
            SetofItemStack stacks = new SetofItemStack(items.tagCount());
            for (int i = 0; i < items.tagCount(); i++) {
                NBTTagCompound c = (NBTTagCompound) items.get(i);
                stacks.add(ItemStack.loadItemStackFromNBT(c));
            }
            return stacks;
        } else {
            return new SetofItemStack();
        }
    }

    private static void setFilterItems(ItemStack filterStack, SetofItemStack items) {
        if (!filterStack.hasTagCompound()) {
            filterStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : items) {
            list.appendTag(stack.serializeNBT());
        }
        NBTTagCompound compound = filterStack.getTagCompound();
        compound.setTag(NBT_ITEMS, list);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        list.add(I18n.format("itemText.misc.bulkItemFilter.count", getSize(stack)));
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.BULKITEM),
                "igi", "mdm", "igi",
                'm', ModItems.blankModule,
                'd', Items.DIAMOND,
                'i', Items.IRON_INGOT,
                'g', Items.GOLD_INGOT);
    }

    @Override
    public Class<? extends GuiScreen> getGuiHandler() {
        return GuiBulkItemFilter.class;
    }

    @Override
    public boolean hasGuiContainer() {
        return false;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float x, float y, float z) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (player.isSneaking()) {
            IItemHandler handler = InventoryUtils.getInventory(world, pos, face);
            if (handler != null) {
                int nAdded = mergeInventory(stack, handler);
                player.addChatMessage(new TextComponentTranslation("chatText.misc.inventoryMerged", nAdded, stack.getDisplayName()));
                world.playSound(null, pos, MRSoundEvents.success, SoundCategory.MASTER, 1.0f, 1.0f);
                return EnumActionResult.SUCCESS;
            } else {
                return super.onItemUse(stack, player, world, pos, hand, face, x, y, z);
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    @Override
    public IMessage dispatchMessage(FilterSettingsMessage message, ItemStack filterStack) {
        IItemHandler srcInv;
        switch (message.getOp()) {
            case CLEAR_ALL:
                setFilterItems(filterStack, new SetofItemStack());
                return null;
            case MERGE:
                srcInv = getInventory(message);
                if (srcInv != null) {
                    int n = mergeInventory(filterStack, srcInv);
                }
                return new GuiSyncMessage(filterStack);
            case LOAD:
                srcInv = getInventory(message);
                if (srcInv != null) {
                    setFilterItems(filterStack, new SetofItemStack());
                    int n = mergeInventory(filterStack, srcInv);
                }
                return new GuiSyncMessage(filterStack);
            case REMOVE_ITEM:
                ItemStack toRemove = ItemStack.loadItemStackFromNBT(message.getExtData());
                if (removeFromFilter(filterStack, toRemove)) {
                    return new GuiSyncMessage(filterStack);
                } else {
                    return null;
                }
            default:
                ModularRouters.logger.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
                break;
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        if (filterStack.hasTagCompound()) {
            return filterStack.getTagCompound().getTagList(NBT_ITEMS, Constants.NBT.TAG_COMPOUND).tagCount();
        } else {
            return 0;
        }
    }

    private boolean removeFromFilter(ItemStack filterStack, ItemStack toRemove) {
        SetofItemStack stacks = getFilterItems(filterStack);
        if (stacks.remove(toRemove)) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (ItemStack stack : stacks) {
                list.appendTag(stack.serializeNBT());
            }
            compound.setTag(NBT_ITEMS, list);
            filterStack.setTagCompound(compound);
            return true;
        } else {
            return false;
        }
    }

    private IItemHandler getInventory(FilterSettingsMessage msg) {
        RouterTarget target = RouterTarget.fromNBT(msg.getExtData());
        World w = DimensionManager.getWorld(target.dimId);
        if (w != null) {
            return InventoryUtils.getInventory(w, target.pos, target.face);
        }
        return null;
    }

    private int mergeInventory(ItemStack filterStack, IItemHandler srcInventory) {
        SetofItemStack stacks = getFilterItems(filterStack);
        int origSize = stacks.size();

        for (int i = 0; i < srcInventory.getSlots() && stacks.size() < MAX_SIZE; i++) {
            ItemStack stack = srcInventory.getStackInSlot(i);
            if (stack != null) {
                stacks.add(stack);
            }
        }

        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : stacks) {
            list.appendTag(stack.serializeNBT());
        }
        compound.setTag(NBT_ITEMS, list);
        filterStack.setTagCompound(compound);

        return list.tagCount() - origSize;
    }
}
