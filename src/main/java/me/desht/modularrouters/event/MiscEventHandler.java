package me.desht.modularrouters.event;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.TemplateFrameBlock;
import me.desht.modularrouters.container.RouterMenu;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.fake_player.RouterFakePlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;


import java.util.Iterator;

@EventBusSubscriber(modid = ModularRouters.MODID)
public class MiscEventHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof RouterFakePlayer fakePlayer) {
            // try to insert as much as possible of the dropped item(s) into the router
            Iterator<ItemEntity> iter = event.getDrops().iterator();
            while (iter.hasNext()) {
                ItemEntity itemEntity = iter.next();
                if (itemEntity.isAlive()) {
                    ItemStack excess = fakePlayer.tryInsertIntoRouter(itemEntity.getItem());
                    if (excess.isEmpty()) {
                        // the whole item could be inserted
                        iter.remove();
                    } else if (excess.getCount() < itemEntity.getItem().getCount()) {
                        // some of the item could be inserted
                        itemEntity.setItem(excess);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDigSpeedCheck(PlayerEvent.BreakSpeed event) {
        if (event.getState().getBlock() instanceof TemplateFrameBlock) {
            event.getPosition().flatMap(pos -> event.getEntity().level().getBlockEntity(pos, ModBlockEntities.TEMPLATE_FRAME.get())).ifPresent(te -> {
                if (te.getCamouflage() != null && te.extendedMimic()) {
                    // note: passing pos here would cause an infinite event loop; necessary to pass null
                    event.setNewSpeed(event.getEntity().getDestroySpeed(te.getCamouflage(), null));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        if (event.getCrafting().getItem() instanceof IPlayerOwned playerOwned) {
            // player-owned items get tagged with the creator's name & ID
            playerOwned.setOwner(stack, event.getEntity());
        } else if (stack.getItem() instanceof ModuleItem) {
            // if self-crafting a module to reset it; retrieve any augments in the module
            ItemStack moduleStack = ItemStack.EMPTY;
            for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
                ItemStack s = event.getInventory().getItem(i);
                if (!s.isEmpty()) {
                    if (s.getItem() == event.getCrafting().getItem()) {
                        moduleStack = s;
                    } else {
                        break;
                    }
                }
            }
            if (!moduleStack.isEmpty()) {
                AugmentHandler h = new AugmentHandler(moduleStack, null);
                for (int i = 0; i < h.getSlots(); i++) {
                    event.getEntity().getInventory().placeItemBackInInventory(h.getStackInSlot(i));
                }
            }
        }
    }
}
