package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.gui.filter.GuiRegexFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.RegexMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.stream.Collectors;

public class RegexFilter extends SmartFilter {
    private static final String NBT_REGEX = "Regex";
    private static final int MAX_SIZE = 6;

    public static List<String> getRegexList(ItemStack filterStack) {
        if (filterStack.hasTagCompound()) {
            NBTTagList items = filterStack.getTagCompound().getTagList(NBT_REGEX, Constants.NBT.TAG_STRING);
            List<String> res = Lists.newArrayListWithExpectedSize(items.tagCount());
            for (int i = 0; i < items.tagCount(); i++) {
                res.add(items.getStringTagAt(i));
            }
            return res;
        } else {
            return Lists.newArrayList();
        }
    }

    public static void setRegexList(ItemStack filterStack, List<String> regex) {
        if (!filterStack.hasTagCompound()) {
            filterStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagList list = new NBTTagList();
        for (String r : regex) {
            list.appendTag(new NBTTagString(r));
        }
        NBTTagCompound compound = filterStack.getTagCompound();
        compound.setTag(NBT_REGEX, list);
    }

    @Override
    protected void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        NBTTagCompound compound = itemstack.getTagCompound();
        if (compound != null) {
            List<String> l = getRegexList(itemstack);
            list.add(I18n.format("itemText.misc.regexFilter.count", l.size()));
            list.addAll(l.stream().map(s -> " \u2022 " + TextFormatting.AQUA + "/" + s + "/").collect(Collectors.toList()));
        } else {
            list.add(I18n.format("itemText.misc.regexFilter.count", 0));
        }
    }

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack, ModuleTarget target) {
        return new RegexMatcher(getRegexList(filterStack));
    }

    @Override
    public Class<? extends GuiScreen> getGuiHandler() {
        return GuiRegexFilter.class;
    }

    @Override
    public boolean hasGuiContainer() {
        return false;
    }

    @Override
    public IMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
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
                int pos = message.getNbtData().getInteger("Pos");
                l = getRegexList(filterStack);
                if (pos >= 0 && pos < l.size()) {
                    l.remove(pos);
                    setRegexList(filterStack, l);
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
        return filterStack.hasTagCompound() ? filterStack.getTagCompound().getTagList(NBT_REGEX, Constants.NBT.TAG_STRING).tagCount() : 0;
    }
}
