package me.desht.modularrouters.integration.jade;

import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadeIntegration implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new RouterDataProvider(), ModularRouterBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new RouterComponentProvider(), ModularRouterBlock.class);
    }
}
