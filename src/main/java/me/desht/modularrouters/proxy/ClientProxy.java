package me.desht.modularrouters.proxy;

import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        super.preInit();
        MinecraftForge.EVENT_BUS.register(new Config.ConfigEventHandler());
    }

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(ModularRouters.modId + ":" + id, "inventory"));
    }
}
