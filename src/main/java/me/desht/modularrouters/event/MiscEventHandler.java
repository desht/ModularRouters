package me.desht.modularrouters.event;

import me.desht.modularrouters.block.BlockTemplateFrame;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod.EventBusSubscriber
public class MiscEventHandler {
    @SubscribeEvent
    public static void onDigSpeedCheck(PlayerEvent.BreakSpeed event) {
        if (event.getPos() != null) {
            BlockState state = event.getPlayer().getEntityWorld().getBlockState(event.getPos());
            if (state.getBlock() instanceof BlockTemplateFrame) {
                TileEntityTemplateFrame.getTemplateFrame(event.getPlayer().getEntityWorld(), event.getPos()).ifPresent(te -> {
                    if (te.getCamouflage() != null && te.extendedMimic()) {
                        BlockState camoState = te.getCamouflage();
                        event.setNewSpeed(event.getPlayer().getDigSpeed(camoState, null));
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack stack = event.getCrafting();
        if (event.getCrafting().getItem() instanceof IPlayerOwned) {
            // player-owned items get tagged with the creator's name & ID
            ((IPlayerOwned) stack.getItem()).setOwner(stack, event.getPlayer());
        } else if (stack.getItem() instanceof ItemModule) {
            // if self-crafting a module to reset it; retrieve any augments in the module
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
                AugmentHandler h = new AugmentHandler(moduleStack, null);
                for (int i = 0; i < h.getSlots(); i++) {
                    ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), h.getStackInSlot(i));
                }
            }
        }
    }
}
