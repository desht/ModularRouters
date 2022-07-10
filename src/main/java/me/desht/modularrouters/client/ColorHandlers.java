package me.desht.modularrouters.client;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorHandlers {
    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            if (item.get() instanceof ModItems.ITintable tintable) {
                event.getItemColors().register((stack, tintIndex) -> switch (tintIndex) {
                    case 0, 2 -> TintColor.WHITE.getRGB();
                    case 1 -> tintable.getItemTint().getRGB();
                    default -> TintColor.BLACK.getRGB();  // shouldn't get here
                }, item.get());
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.getBlockColors().register((state, reader, pos, tintIndex) -> {
            if (pos == null || reader == null) return -1;
            BlockEntity te = reader.getBlockEntity(pos);
            if (te instanceof ICamouflageable camouflageable && camouflageable.getCamouflage() != null) {
                return event.getBlockColors().getColor(camouflageable.getCamouflage(), te.getLevel(), pos, tintIndex);
            } else {
                return 0xffffff;
            }
        }, ModBlocks.MODULAR_ROUTER.get(), ModBlocks.TEMPLATE_FRAME.get());
    }
}
