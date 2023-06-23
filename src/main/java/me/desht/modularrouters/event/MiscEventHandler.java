package me.desht.modularrouters.event;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.TemplateFrameBlock;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID)
public class MiscEventHandler {
    @SubscribeEvent
    public static void onDigSpeedCheck(PlayerEvent.BreakSpeed event) {
        if (event.getState().getBlock() instanceof TemplateFrameBlock) {
            event.getPosition().flatMap(pos -> event.getEntity().level().getBlockEntity(pos, ModBlockEntities.TEMPLATE_FRAME.get())).ifPresent(te -> {
                if (te.getCamouflage() != null && te.extendedMimic()) {
                    // note: passing pos here would cause an infinite event loop; necessary to pass null
                    event.setNewSpeed(event.getEntity().getDigSpeed(te.getCamouflage(), null));
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
                    ItemHandlerHelper.giveItemToPlayer(event.getEntity(), h.getStackInSlot(i));
                }
            }
        }
    }

}
