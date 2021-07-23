package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.Comparison;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.ComparisonList;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class InspectionFilter extends SmartFilterItem {
    private static final String NBT_MATCH_ALL = "MatchAll";
    private static final String NBT_ITEMS = "Items";
    public static final int MAX_SIZE = 6;

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new InspectionMatcher(getComparisonList(filterStack));
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<Component> list) {
        super.addExtraInformation(itemstack, list);
        ComparisonList comparisonList = getComparisonList(itemstack);
        if (!comparisonList.items.isEmpty()) {
            list.add(new TextComponent(ChatFormatting.YELLOW + I18n.get("modularrouters.guiText.label.matchAll." + comparisonList.isMatchAll()) + ":"));
            for (Comparison c : comparisonList.items) {
                list.add(new TextComponent(ChatFormatting.AQUA + "\u2022 " + c.asLocalizedText()));
            }
        }
    }

    public static ComparisonList getComparisonList(ItemStack filterStack) {
        CompoundTag compound = filterStack.getTagElement(ModularRouters.MODID);
        if (compound != null) {
            boolean matchAll = compound.getBoolean(NBT_MATCH_ALL);
            List<Comparison> l = Lists.newArrayList();
            ListTag items = compound.getList(NBT_ITEMS, Constants.NBT.TAG_STRING);
            for (int i = 0; i < items.size(); i++) {
                l.add(Comparison.fromString(items.getString(i)));
            }
            return new ComparisonList(l, matchAll);
        } else {
            return new ComparisonList(Lists.newArrayList(), false);
        }
    }

    private void setComparisonList(ItemStack filterStack, ComparisonList comparisonList) {
        ListTag l = comparisonList.items.stream().map(comp -> StringTag.valueOf(comp.toString())).collect(Collectors.toCollection(ListTag::new));
        CompoundTag compound = filterStack.getOrCreateTagElement(ModularRouters.MODID);
        compound.putBoolean(NBT_MATCH_ALL, comparisonList.isMatchAll());
        compound.put(NBT_ITEMS, l);
    }

    @Override
    public GuiSyncMessage onReceiveSettingsMessage(Player player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        ComparisonList comparisonList = getComparisonList(filterStack);

        switch (message.getOp()) {
            case ADD_STRING:
                if (comparisonList.items.size() < MAX_SIZE) {
                    Comparison c = Comparison.fromString(message.getPayload().getString("Comparison"));
                    comparisonList.items.add(c);
                    setComparisonList(filterStack, comparisonList);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case REMOVE_AT:
                int pos = message.getPayload().getInt("Pos");
                if (pos >= 0 && pos < comparisonList.items.size()) {
                    comparisonList.items.remove(pos);
                    setComparisonList(filterStack, comparisonList);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case ANY_ALL_FLAG:
                comparisonList.setMatchAll(message.getPayload().getBoolean("MatchAll"));
                setComparisonList(filterStack, comparisonList);
                return new GuiSyncMessage(filterStack);
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        return 0;
    }
}
