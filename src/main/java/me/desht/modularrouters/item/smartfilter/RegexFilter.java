package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.filter.GuiRegexFilter;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.RegexMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class RegexFilter extends ItemSmartFilter {
    private static final String NBT_REGEX = "Regex";
    private static final int MAX_SIZE = 6;

    public RegexFilter(Properties props) {
        super(props);
    }

    public static List<String> getRegexList(ItemStack filterStack) {
        if (filterStack.hasTag()) {
            NBTTagList items = filterStack.getTag().getList(NBT_REGEX, Constants.NBT.TAG_STRING);
            List<String> res = Lists.newArrayListWithExpectedSize(items.size());
            for (int i = 0; i < items.size(); i++) {
                res.add(items.getString(i));
            }
            return res;
        } else {
            return Lists.newArrayList();
        }
    }

    public static void setRegexList(ItemStack filterStack, List<String> regex) {
        NBTTagList list = regex.stream().map(NBTTagString::new).collect(Collectors.toCollection(NBTTagList::new));
        filterStack.getOrCreateTag().put(NBT_REGEX, list);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addExtraInformation(itemstack, list);
        NBTTagCompound compound = itemstack.getTag();
        if (compound != null) {
            List<String> l = getRegexList(itemstack);
            list.add(new TextComponentTranslation("itemText.misc.regexFilter.count", l.size()));
            list.addAll(l.stream().map(s -> " \u2022 " + TextFormatting.AQUA + "/" + s + "/").map(TextComponentString::new).collect(Collectors.toList()));
        } else {
            list.add(new TextComponentTranslation("itemText.misc.regexFilter.count", 0));
        }
    }

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new RegexMatcher(getRegexList(filterStack));
    }

    @Override
    public Class<? extends GuiScreen> getGuiClass() {
        return GuiRegexFilter.class;
    }

    @Override
    public boolean hasContainer() {
        return false;
    }

    @Override
    public GuiSyncMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        List<String> l;
        switch (message.getOp()) {
            case ADD_STRING:
                String regex = message.getNbtData().getString("String");
                l = getRegexList(filterStack);
                if (l.size() < MAX_SIZE) {
                    l.add(regex);
                    setRegexList(filterStack, l);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case REMOVE_AT:
                int pos = message.getNbtData().getInt("Pos");
                l = getRegexList(filterStack);
                if (pos >= 0 && pos < l.size()) {
                    l.remove(pos);
                    setRegexList(filterStack, l);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            default:
                ModularRouters.LOGGER.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
                break;
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return filterStack.hasTag() ? filterStack.getTag().getList(NBT_REGEX, Constants.NBT.TAG_STRING).size() : 0;
    }
}
