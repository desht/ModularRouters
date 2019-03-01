package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.filter.GuiInspectionFilter;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.Comparison;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.ComparisonList;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class InspectionFilter extends ItemSmartFilter {
    private static final String NBT_MATCH_ALL = "MatchAll";
    private static final String NBT_ITEMS = "Items";
    public static final int MAX_SIZE = 6;

    public InspectionFilter(Properties props) {
        super(props);
    }

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new InspectionMatcher(getComparisonList(filterStack));
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addExtraInformation(itemstack, list);
        ComparisonList comparisonList = getComparisonList(itemstack);
        if (!comparisonList.items.isEmpty()) {
            list.add(new TextComponentString(TextFormatting.YELLOW + I18n.format("guiText.label.matchAll." + comparisonList.isMatchAll()) + ":"));
            for (Comparison c : comparisonList.items) {
                list.add(new TextComponentString(TextFormatting.AQUA + "\u2022 " + c.asLocalizedText()));
            }
        }
    }

    public static ComparisonList getComparisonList(ItemStack filterStack) {
        if (filterStack.hasTag()) {
            NBTTagCompound compound = filterStack.getTag();
            boolean matchAll = compound.getBoolean(NBT_MATCH_ALL);
            List<Comparison> l = Lists.newArrayList();
            NBTTagList items = compound.getList(NBT_ITEMS, Constants.NBT.TAG_STRING);
            for (int i = 0; i < items.size(); i++) {
                l.add(Comparison.fromString(items.getString(i)));
            }
            return new ComparisonList(l, matchAll);
        } else {
            return new ComparisonList(Lists.newArrayList(), false);
        }
    }

    private void setComparisonList(ItemStack filterStack, ComparisonList comparisonList) {
        NBTTagList l = comparisonList.items.stream().map(comp -> new NBTTagString(comp.toString())).collect(Collectors.toCollection(NBTTagList::new));
        NBTTagCompound compound = filterStack.getOrCreateTag();
        compound.putBoolean(NBT_MATCH_ALL, comparisonList.isMatchAll());
        compound.put(NBT_ITEMS, l);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<? extends GuiScreen> getGuiClass() {
        return GuiInspectionFilter.class;
    }

    @Override
    public GuiSyncMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        ComparisonList comparisonList = getComparisonList(filterStack);

        switch (message.getOp()) {
            case ADD_STRING:
                if (comparisonList.items.size() < MAX_SIZE) {
                    Comparison c = Comparison.fromString(message.getNbtData().getString("Comparison"));
                    comparisonList.items.add(c);
                    setComparisonList(filterStack, comparisonList);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case REMOVE_AT:
                int pos = message.getNbtData().getInt("Pos");
                if (pos >= 0 && pos < comparisonList.items.size()) {
                    comparisonList.items.remove(pos);
                    setComparisonList(filterStack, comparisonList);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case ANY_ALL_FLAG:
                comparisonList.setMatchAll(message.getNbtData().getBoolean("MatchAll"));
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
