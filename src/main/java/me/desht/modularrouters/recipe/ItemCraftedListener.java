package me.desht.modularrouters.recipe;

import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber
class ItemCraftedListener {
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        // security upgrade and player modules need to be tagged with the player when crafted
        if (ItemUpgrade.isType(event.crafting, ItemUpgrade.UpgradeType.SECURITY) || ModuleHelper.isModuleType(event.crafting, ItemModule.ModuleType.PLAYER)) {
            ModuleHelper.setOwner(event.crafting, event.player);
        }

        // if self-crafting a module to reset it, retrieve any augments in the module
        if (event.crafting.getItem() instanceof ItemModule) {
            ItemStack moduleStack = ItemStack.EMPTY;
            for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
                ItemStack s = event.craftMatrix.getStackInSlot(i);
                if (!s.isEmpty()) {
                    if (s.getItem() == event.crafting.getItem()) {
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
                        if (!event.player.addItemStackToInventory(s)) {
                            InventoryUtils.dropItems(event.player.getEntityWorld(), event.player.getPosition(), s);
                        }
                    }
                }
            }
        }

    }
}
