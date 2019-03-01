package me.desht.modularrouters.client;

import me.desht.modularrouters.block.tile.ICamouflageable;
import me.desht.modularrouters.core.ITintable;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.awt.*;

public class ColorHandlers {
    @SubscribeEvent
    public static void registerItemColorHandlers(ColorHandlerEvent.Item event) {
        for (IForgeRegistryEntry entry : ObjectRegistry.registeredObjects) {
            if (entry instanceof Item && entry instanceof ITintable) {
                event.getItemColors().register((stack, tintIndex) -> {
                    switch(tintIndex) {
                        case 0: case 2: return Color.WHITE.getRGB();
                        case 1:
                            return ((ITintable) stack.getItem()).getItemTint().getRGB();
                        default:
                            return Color.BLACK.getRGB();  // shouldn't get here
                    }
                }, (Item) entry);
            }
        }
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((state, reader, pos, tintIndex) -> {
            if (pos == null) return -1;
            TileEntity te = MiscUtil.getTileEntitySafely(reader, pos);
            if (te instanceof ICamouflageable && ((ICamouflageable) te).getCamouflage() != null) {
                return event.getBlockColors().getColor(((ICamouflageable) te).getCamouflage(), te.getWorld(), pos, tintIndex);
            } else {
                return 0xffffff;
            }
        });
    }
}
