package me.desht.modularrouters.integration.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import me.desht.modularrouters.block.BlockItemRouter;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class WailaIntegration {
    public static void setup() {
        FMLInterModComms.sendMessage("Waila", "register", "me.desht.modularrouters.integration.waila.WailaIntegration.callback");
    }

    public static void callback(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new RouterDataProvider(), BlockItemRouter.class);
    }
}

