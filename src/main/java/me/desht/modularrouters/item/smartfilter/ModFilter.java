package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.filter.GuiModFilter;
import me.desht.modularrouters.container.ContainerModFilter;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.ModMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class ModFilter extends ItemSmartFilter {
    private static final String NBT_MODS = "Mods";
    public static final int MAX_SIZE = 6;

    public ModFilter(Properties props) {
        super(props);
    }

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new ModMatcher(getModList(filterStack));
    }

    public static List<String> getModList(ItemStack filterStack) {
        if (filterStack.hasTag()) {
            NBTTagList items = filterStack.getTag().getList(NBT_MODS, Constants.NBT.TAG_STRING);
            List<String> res = Lists.newArrayListWithExpectedSize(items.size());
            for (int i = 0; i < items.size(); i++) {
                res.add(items.getString(i));
            }
            return res;
        } else {
            return Lists.newArrayList();
        }
    }

    private static void setModList(ItemStack filterStack, List<String> mods) {
        NBTTagList list = mods.stream().map(NBTTagString::new).collect(Collectors.toCollection(NBTTagList::new));
        filterStack.getOrCreateTag().put(NBT_MODS, list);
    }

    @Override
    public void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
        super.addExtraInformation(stack, list);
        NBTTagCompound compound = stack.getTag();
        if (compound != null) {
            List<String> l = getModList(stack);
            list.add(new TextComponentTranslation("itemText.misc.modFilter.count", l.size()));
            list.addAll(l.stream()
                    .map(ModNameCache::getModName)
                    .map(s -> " \u2022 " + TextFormatting.AQUA + s)
                    .map(TextComponentString::new)
                    .collect(Collectors.toList()));
        } else {
            list.add(new TextComponentTranslation("itemText.misc.modFilter.count", 0));
        }
    }

    @Override
    public Class<? extends GuiScreen> getGuiClass() {
        return GuiModFilter.class;
    }

    @Override
    public boolean hasContainer() {
        return true;
    }

    @Override
    public ContainerSmartFilter createContainer(EntityPlayer player, EnumHand hand, TileEntityItemRouter router) {
        return new ContainerModFilter(player, hand, router);
    }

    @Override
    public GuiSyncMessage dispatchMessage(EntityPlayer player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
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
                int pos = message.getNbtData().getInt("Pos");
                l = getModList(filterStack);
                if (pos >= 0 && pos < l.size()) {
                    l.remove(pos);
                    setModList(filterStack, l);
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
        return filterStack.hasTag() ? filterStack.getTag().getList(NBT_MODS, Constants.NBT.TAG_STRING).size() : 0;
    }
}
