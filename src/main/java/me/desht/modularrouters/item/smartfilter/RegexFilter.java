package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.gui.filter.GuiRegexFilter;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.RegexMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class RegexFilter extends ItemSmartFilter {
    private static final String NBT_REGEX = "Regex";
    public static final int MAX_SIZE = 6;

    public RegexFilter(Properties props) {
        super(props);
    }

    public static List<String> getRegexList(ItemStack filterStack) {
        if (filterStack.hasTag()) {
            ListNBT items = filterStack.getTag().getList(NBT_REGEX, Constants.NBT.TAG_STRING);
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
        ListNBT list = regex.stream().map(StringNBT::new).collect(Collectors.toCollection(ListNBT::new));
        filterStack.getOrCreateTag().put(NBT_REGEX, list);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addExtraInformation(itemstack, list);
        CompoundNBT compound = itemstack.getTag();
        if (compound != null) {
            List<String> l = getRegexList(itemstack);
            list.add(new TranslationTextComponent("itemText.misc.regexFilter.count", l.size()));
            list.addAll(l.stream().map(s -> " \u2022 " + TextFormatting.AQUA + "/" + s + "/").map(StringTextComponent::new).collect(Collectors.toList()));
        } else {
            list.add(new TranslationTextComponent("itemText.misc.regexFilter.count", 0));
        }
    }

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new RegexMatcher(getRegexList(filterStack));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<? extends Screen> getGuiClass() {
        return GuiRegexFilter.class;
    }

    @Override
    public boolean hasContainer() {
        return false;
    }

    @Override
    public GuiSyncMessage onReceiveSettingsMessage(PlayerEntity player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        List<String> l;
        switch (message.getOp()) {
            case ADD_STRING:
                String regex = message.getPayload().getString("String");
                l = getRegexList(filterStack);
                if (l.size() < MAX_SIZE) {
                    l.add(regex);
                    setRegexList(filterStack, l);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case REMOVE_AT:
                int pos = message.getPayload().getInt("Pos");
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
