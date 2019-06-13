package me.desht.modularrouters.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

public interface IPlayerOwned {
    String NBT_OWNER = "Owner";

    default String getOwnerName(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(NBT_OWNER)) return null;

        ListNBT l = stack.getTag().getList(NBT_OWNER, Constants.NBT.TAG_STRING);
        return l.getString(0);
    }

    default UUID getOwnerID(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(NBT_OWNER)) return null;

        ListNBT l = stack.getTag().getList(NBT_OWNER, Constants.NBT.TAG_STRING);
        return UUID.fromString(l.getString(1));
    }

    default void setOwner(ItemStack stack, PlayerEntity player) {
        CompoundNBT compound = stack.getOrCreateTag();
        ListNBT owner = new ListNBT();
        owner.add(new StringNBT(player.getDisplayName().getString()));
        owner.add(new StringNBT(player.getUniqueID().toString()));
        compound.put(NBT_OWNER, owner);
        stack.setTag(compound);
    }
}
