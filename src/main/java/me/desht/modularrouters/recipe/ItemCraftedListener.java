package me.desht.modularrouters.recipe;

import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.PlayerModule;
import me.desht.modularrouters.item.upgrade.SecurityUpgrade;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber
class ItemCraftedListener {
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getCrafting().getItem() instanceof SecurityUpgrade || event.getCrafting().getItem() instanceof PlayerModule) {
            // security upgrade and player modules need to be tagged with the player when crafted
            ModuleHelper.setOwner(event.getCrafting(), event.getPlayer());
        } else if (event.getCrafting().getItem() instanceof ItemModule) {
            // if self-crafting a module to reset it, retrieve any augments in the module
            ItemStack moduleStack = ItemStack.EMPTY;
            for (int i = 0; i < event.getInventory().getSizeInventory(); i++) {
                ItemStack s = event.getInventory().getStackInSlot(i);
                if (!s.isEmpty()) {
                    if (s.getItem() == event.getCrafting().getItem()) {
                        moduleStack = s;
                    } else {
                        break;
                    }
                }
            }
            if (!moduleStack.isEmpty()) {
                AugmentHandler h = new AugmentHandler(moduleStack);
                for (int i = 0; i < h.getSlots(); i++) {
                    ItemStack s = h.getStackInSlot(i);
                    if (!s.isEmpty()) {
                        if (!event.getPlayer().addItemStackToInventory(s)) {
                            InventoryUtils.dropItems(event.getPlayer().getEntityWorld(), event.getPlayer().getPosition(), s);
                        }
                    }
                }
            }
        }
    }
}
