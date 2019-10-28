package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.BlockTemplateFrame;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.modularrouters.util.MiscUtil.RL;

@ObjectHolder(ModularRouters.MODID)
public class ModBlocks {
    public static final BlockItemRouter ITEM_ROUTER = null;
    public static final BlockTemplateFrame TEMPLATE_FRAME = null;

    @Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(new BlockItemRouter().setRegistryName(RL("item_router")));
            event.getRegistry().register(new BlockTemplateFrame().setRegistryName(RL("template_frame")));
        }
    }
}
