package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerModFilter;
import me.desht.modularrouters.gui.filter.GuiModFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.ModMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;
import java.util.stream.Collectors;

public class ModFilter extends SmartFilter {
    private static final String NBT_MODS = "Mods";
    private static final int MAX_SIZE = 6;

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack, ModuleTarget target) {
        return new ModMatcher(getModList(filterStack));
    }

    public static List<String> getModList(ItemStack filterStack) {
        if (filterStack.hasTagCompound()) {
            NBTTagList items = filterStack.getTagCompound().getTagList(NBT_MODS, Constants.NBT.TAG_STRING);
            List<String> res = Lists.newArrayListWithExpectedSize(items.tagCount());
            for (int i = 0; i < items.tagCount(); i++) {
                res.add(items.getStringTagAt(i));
            }
            return res;
        } else {
            return Lists.newArrayList();
        }
    }

    private static void setModList(ItemStack filterStack, List<String> mods) {
        if (!filterStack.hasTagCompound()) {
            filterStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagList list = new NBTTagList();
        for (String m : mods) {
            list.appendTag(new NBTTagString(m));
        }
        NBTTagCompound compound = filterStack.getTagCompound();
        compound.setTag(NBT_MODS, list);

    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.MOD),
                ItemSmartFilter.makeItemStack(ItemSmartFilter.FilterType.BULKITEM),
                Items.REPEATER, Blocks.REDSTONE_TORCH);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            List<String> l = getModList(stack);
            list.add(I18n.format("itemText.misc.modFilter.count", l.size()));
            list.addAll(l.stream().map(ModNameCache::getModName).map(s -> " \u2022 " + TextFormatting.AQUA + s).collect(Collectors.toList()));
        } else {
            list.add(I18n.format("itemText.misc.modFilter.count", 0));
        }
    }

    @Override
    public Class<? extends GuiScreen> getGuiHandler() {
        return GuiModFilter.class;
    }

    @Override
    public boolean hasGuiContainer() {
        return true;
    }

    @Override
    public Container createContainer(EntityPlayer player, ItemStack filterStack, TileEntityItemRouter router) {
        return new ContainerModFilter(player, filterStack, router);
    }

    @Override
    public IMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        List<String> l;
        switch (message.getOp()) {
            case ADD_STRING:
                String modId = message.getNbtData().getString("ModId");
                l = getModList(filterStack);
                if (l.size() < MAX_SIZE && !l.contains(modId)) {
                    l.add(modId);
                    setModList(filterStack, l);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case REMOVE_AT:
                int pos = message.getNbtData().getInteger("Pos");
                l = getModList(filterStack);
                if (pos >= 0 && pos < l.size()) {
                    l.remove(pos);
                    setModList(filterStack, l);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            default:
                ModularRouters.logger.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
                break;
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return filterStack.hasTagCompound() ? filterStack.getTagCompound().getTagList(NBT_MODS, Constants.NBT.TAG_STRING).tagCount() : 0;
    }
}
