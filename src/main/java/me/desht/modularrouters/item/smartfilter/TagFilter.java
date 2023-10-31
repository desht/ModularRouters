package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.AbstractSmartFilterMenu;
import me.desht.modularrouters.container.TagFilterMenu;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.TagMatcher;
import me.desht.modularrouters.network.FilterSettingsMessage;
import me.desht.modularrouters.network.GuiSyncMessage;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagFilter extends SmartFilterItem {
    private static final int MAX_SIZE = 6;
    private static final String NBT_TAGS = "Tags";

    public static List<TagKey<Item>> getTagList(ItemStack filterStack) {
        CompoundTag tag = filterStack.getTagElement(ModularRouters.MODID);
        if (tag != null) {
            return tag.getList(NBT_TAGS, Tag.TAG_STRING).stream()
                    .map(strTag -> TagKey.create(Registries.ITEM, new ResourceLocation(strTag.getAsString())))
                    .toList();
        } else {
            return List.of();
        }
    }

    public static void setTagList(ItemStack filterStack, List<TagKey<Item>> tags) {
        ListTag list = tags.stream()
                .map(tag -> StringTag.valueOf(tag.location().toString()))
                .collect(Collectors.toCollection(ListTag::new));
        filterStack.getOrCreateTagElement(ModularRouters.MODID).put(NBT_TAGS, list);
    }

    @NotNull
    @Override
    public IItemMatcher compile(ItemStack filterStack, ItemStack moduleStack) {
        return new TagMatcher(getTagList(filterStack));
    }

    @Override
    public void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);
        if (stack.getTagElement(ModularRouters.MODID) != null) {
            List<TagKey<Item>> l = getTagList(stack);
            list.add(ClientUtil.xlate("modularrouters.itemText.misc.filter.count", l.size()));
            list.addAll(l.stream()
                    .map(s -> " • " + ChatFormatting.AQUA + s.location())
                    .map(Component::literal)
                    .toList());
        } else {
            list.add(ClientUtil.xlate("modularrouters.itemText.misc.filter.count", 0));
        }
    }

    @Nullable
    @Override
    public GuiSyncMessage onReceiveSettingsMessage(Player player, FilterSettingsMessage message, ItemStack filterStack, ItemStack moduleStack) {
        List<TagKey<Item>> tagList;
        switch (message.getOp()) {
            case ADD_STRING -> {
                tagList = new ArrayList<>(getTagList(filterStack));
                String t = message.getPayload().getString("Tag");
                if (tagList.size() < MAX_SIZE && ResourceLocation.isValidResourceLocation(t)) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(t));
                    if (!tagList.contains(tag)) {
                        tagList.add(tag);
                        setTagList(filterStack, tagList);
                        return new GuiSyncMessage(filterStack);
                    }
                }
            }
            case REMOVE_AT -> {
                int pos = message.getPayload().getInt("Pos");
                tagList = new ArrayList<>(getTagList(filterStack));
                if (pos >= 0 && pos < tagList.size()) {
                    tagList.remove(pos);
                    setTagList(filterStack, tagList);
                    return new GuiSyncMessage(filterStack);
                }
            }
            default -> ModularRouters.LOGGER.warn("received unexpected message type " + message.getOp() + " for " + filterStack);
        }
        return null;
    }

    @Override
    public int getSize(ItemStack filterStack) {
        CompoundTag tag = filterStack.getTagElement(ModularRouters.MODID);
        return tag != null ? tag.getList(NBT_TAGS, Tag.TAG_STRING).size() : 0;
    }

    @Override
    public AbstractSmartFilterMenu createMenu(int windowId, Inventory invPlayer, MFLocator loc) {
        return new TagFilterMenu(windowId, invPlayer, loc);
    }
}
