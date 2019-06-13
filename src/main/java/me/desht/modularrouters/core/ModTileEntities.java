package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.modularrouters.util.MiscUtil.RL;

@ObjectHolder(ModularRouters.MODID)
@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTileEntities {
    public static final TileEntityType<?> ITEM_ROUTER = null;
    public static final TileEntityType<?> TEMPLATE_FRAME = null;

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(TileEntityItemRouter::new, ModBlocks.ITEM_ROUTER).build(null).setRegistryName(RL("item_router")));
        event.getRegistry().register(TileEntityType.Builder.create(TileEntityTemplateFrame::new, ModBlocks.TEMPLATE_FRAME).build(null).setRegistryName(RL("template_frame")));
    }
}
