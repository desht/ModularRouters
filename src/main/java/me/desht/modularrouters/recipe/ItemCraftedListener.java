package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ItemCraftedListener {
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (ItemUpgrade.isType(event.crafting, ItemUpgrade.UpgradeType.SECURITY)) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList owner = new NBTTagList();
            owner.appendTag(new NBTTagString(event.player.getDisplayNameString()));
            owner.appendTag(new NBTTagString(event.player.getUniqueID().toString()));
            compound.setTag("Owner", owner);
            event.crafting.setTagCompound(compound);
        }
    }
}
