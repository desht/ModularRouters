package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.gui.filter.GuiInspectionFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.Comparison;
import me.desht.modularrouters.logic.filter.matchers.InspectionMatcher.ComparisonList;
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

public class InspectionFilter extends SmartFilter {
    private static final String NBT_MATCH_ALL = "MatchAll";
    private static final String NBT_ITEMS = "Items";
    private static final int MAX_SIZE = 6;

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack, ModuleTarget target) {
        return new InspectionMatcher(getComparisonList(filterStack));
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        ComparisonList comparisonList = getComparisonList(itemstack);
        if (!comparisonList.items.isEmpty()) {
            list.add(TextFormatting.YELLOW + I18n.format("guiText.label.matchAll." + comparisonList.isMatchAll()) + ":");
            for (Comparison c : comparisonList.items) {
                list.add(TextFormatting.AQUA + "\u2022 " + c.asLocalizedText());
            }
        }
    }

    public static ComparisonList getComparisonList(ItemStack filterStack) {
        if (filterStack.hasTagCompound()) {
            NBTTagCompound compound = filterStack.getTagCompound();
            boolean matchAll = compound.getBoolean(NBT_MATCH_ALL);
            List<Comparison> l = Lists.newArrayList();
            NBTTagList items = compound.getTagList(NBT_ITEMS, Constants.NBT.TAG_STRING);
            for (int i = 0; i < items.tagCount(); i++) {
                l.add(Comparison.fromString(items.getStringTagAt(i)));
            }
            return new ComparisonList(l, matchAll);
        } else {
            return new ComparisonList(Lists.newArrayList(), false);
        }
    }

    private void setComparisonList(ItemStack filterStack, ComparisonList comparisonList) {
        if (!filterStack.hasTagCompound()) {
            filterStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = filterStack.getTagCompound();
        NBTTagList l = new NBTTagList();
        for (Comparison comp : comparisonList.items) {
            l.appendTag(new NBTTagString(comp.toString()));
        }
        compound.setBoolean(NBT_MATCH_ALL, comparisonList.isMatchAll());
        compound.setTag(NBT_ITEMS, l);
    }

    @Override
    public Class<? extends GuiScreen> getGuiHandler() {
        return GuiInspectionFilter.class;
    }

    @Override
    public IMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
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
                int pos = message.getNbtData().getInteger("Pos");
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
