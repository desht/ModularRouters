package me.desht.modularrouters.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.UUID;

public interface IPlayerOwned {
    String NBT_OWNER = "Owner";

    default String getOwnerName(ItemStack stack) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains(NBT_OWNER)) return null;

        ListTag l = stack.getTag().getList(NBT_OWNER, Tag.TAG_STRING);
        return l.getString(0);
    }

    default UUID getOwnerID(ItemStack stack) {
        if (!stack.hasTag() || !Objects.requireNonNull(stack.getTag()).contains(NBT_OWNER)) return null;

        ListTag l = stack.getTag().getList(NBT_OWNER, Tag.TAG_STRING);
        return UUID.fromString(l.getString(1));
    }

    default void setOwner(ItemStack stack, Player player) {
        CompoundTag compound = stack.getOrCreateTag();
        ListTag owner = new ListTag();
        owner.add(StringTag.valueOf(player.getDisplayName().getString()));
        owner.add(StringTag.valueOf(player.getUUID().toString()));
        compound.put(NBT_OWNER, owner);
        stack.setTag(compound);
    }
}
