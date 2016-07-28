package me.desht.modularrouters.integration;

import mcp.mobius.waila.api.IWailaRegistrar;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.RouterDataProvider;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class WailaIntegration {
    public static void setup() {
        FMLInterModComms.sendMessage("Waila", "register", "me.desht.modularrouters.integration.WailaIntegration.callback");
    }

    public static void callback(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new RouterDataProvider(), BlockItemRouter.class);
    }
}

