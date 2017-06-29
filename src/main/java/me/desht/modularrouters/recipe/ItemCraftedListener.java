package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.util.ModuleHelper;
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
    }
}
