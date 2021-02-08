package me.desht.modularrouters.event;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.ValidateModuleMessage;
import me.desht.modularrouters.util.Scheduler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ModularRouters.MODID)
public class ClientEventHandler {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Scheduler.client().tick();
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getWorld().isRemote && event.getItemStack().getItem() instanceof TargetedModule) {
            PacketHandler.NETWORK.sendToServer(new ValidateModuleMessage(event.getHand()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getWorld().isRemote && event.getItemStack().getItem() instanceof TargetedModule) {
            PacketHandler.NETWORK.sendToServer(new ValidateModuleMessage(event.getHand()));
        }
    }
}