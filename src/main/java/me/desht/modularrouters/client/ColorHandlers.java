package me.desht.modularrouters.client;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ColorHandlers {
    @SubscribeEvent
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        for (RegistryObject<Item> item : ModItems.ITEMS.getEntries()) {
            if (item.get() instanceof ModItems.ITintable) {
                event.getItemColors().register((stack, tintIndex) -> {
                    switch (tintIndex) {
                        case 0:
                        case 2:
                            return TintColor.WHITE.getRGB();
                        case 1:
                            return ((ModItems.ITintable) stack.getItem()).getItemTint().getRGB();
                        default:
                            return TintColor.BLACK.getRGB();  // shouldn't get here
                    }
                }, item.get());
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((state, reader, pos, tintIndex) -> {
            if (pos == null || reader == null) return -1;
            TileEntity te = reader.getBlockEntity(pos);
            if (te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null) {
                return event.getBlockColors().getColor(((ICamouflageable) te).getCamouflage(), te.getLevel(), pos, tintIndex);
            } else {
                return 0xffffff;
            }
        }, ModBlocks.ITEM_ROUTER.get(), ModBlocks.TEMPLATE_FRAME.get());
    }
}
