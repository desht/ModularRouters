package me.desht.modularrouters.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

public interface IPlayerOwned {
    public static final String NBT_OWNER = "Owner";

    default String getOwnerName(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(NBT_OWNER)) return null;

        NBTTagList l = stack.getTag().getList(NBT_OWNER, Constants.NBT.TAG_STRING);
        return l.getString(0);
    }

    default UUID getOwnerID(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(NBT_OWNER)) return null;

        NBTTagList l = stack.getTag().getList(NBT_OWNER, Constants.NBT.TAG_STRING);
        return UUID.fromString(l.getString(1));
    }

    default void setOwner(ItemStack stack, EntityPlayer player) {
        NBTTagCompound compound = stack.getOrCreateTag();
        NBTTagList owner = new NBTTagList();
        owner.add(new NBTTagString(player.getDisplayName().getString()));
        owner.add(new NBTTagString(player.getUniqueID().toString()));
        compound.put(NBT_OWNER, owner);
        stack.setTag(compound);
    }
}
