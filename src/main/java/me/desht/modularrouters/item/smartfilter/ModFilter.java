package me.desht.modularrouters.item.smartfilter;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ContainerModFilter;
import me.desht.modularrouters.container.ContainerSmartFilter;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.ModMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class ModFilter extends ItemSmartFilter {
    private static final String NBT_MODS = "Mods";
    public static final int MAX_SIZE = 6;

    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new ModMatcher(getModList(filterStack));
    }

    public static List<String> getModList(ItemStack filterStack) {
        CompoundNBT tag = filterStack.getTagElement(ModularRouters.MODID);
        if (tag != null) {
            ListNBT items = tag.getList(NBT_MODS, Constants.NBT.TAG_STRING);
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
        ListNBT list = mods.stream().map(StringNBT::valueOf).collect(Collectors.toCollection(ListNBT::new));
        filterStack.getOrCreateTagElement(ModularRouters.MODID).put(NBT_MODS, list);
    }

    @Override
    public void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
        super.addExtraInformation(stack, list);
        if (stack.getTagElement(ModularRouters.MODID) != null) {
            List<String> l = getModList(stack);
            list.add(ClientUtil.xlate("modularrouters.itemText.misc.modFilter.count", l.size()));
            list.addAll(l.stream()
                    .map(ModNameCache::getModName)
                    .map(s -> " \u2022 " + TextFormatting.AQUA + s)
                    .map(StringTextComponent::new)
                    .collect(Collectors.toList()));
        } else {
            list.add(ClientUtil.xlate("modularrouters.itemText.misc.modFilter.count", 0));
        }
    }

    @Override
    public boolean hasContainer() {
        return true;
    }

    @Override
    public ContainerSmartFilter createContainer(int windowId, PlayerInventory invPlayer, MFLocator loc) {
        return new ContainerModFilter(windowId, invPlayer, loc);
    }

    @Override
    public GuiSyncMessage onReceiveSettingsMessage(PlayerEntity player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        List<String> l;
        switch (message.getOp()) {
            case ADD_STRING:
                String modId = message.getPayload().getString("ModId");
                l = getModList(filterStack);
                if (l.size() < MAX_SIZE && !l.contains(modId)) {
                    l.add(modId);
                    setModList(filterStack, l);
                    return new GuiSyncMessage(filterStack);
                }
                break;
            case REMOVE_AT:
                int pos = message.getPayload().getInt("Pos");
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
    public int getSize(ItemStack filterStack) {CompoundNBT tag = filterStack.getTagElement(ModularRouters.MODID);
        return tag != null ? tag.getList(NBT_MODS, Constants.NBT.TAG_STRING).size() : 0;
    }
}
