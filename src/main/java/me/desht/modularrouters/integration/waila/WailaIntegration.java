package me.desht.modularrouters.integration.waila;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import me.desht.modularrouters.block.BlockItemRouter;

@WailaPlugin
public class WailaIntegration implements IWailaPlugin {
    @Override
    public void register(IRegistrar iRegistrar) {
        iRegistrar.registerBlockDataProvider(new RouterDataProvider(), BlockItemRouter.class);
        iRegistrar.registerComponentProvider(new RouterComponentProvider(), TooltipPosition.BODY, BlockItemRouter.class);
    }
}
