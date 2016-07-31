package me.desht.modularrouters;

import me.desht.modularrouters.proxy.CommonProxy;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModularRouters.modId, version = ModularRouters.version, name = ModularRouters.name,
        guiFactory = ModularRouters.GUIFACTORY, dependencies = "after:Waila")
public class ModularRouters {
    public static final String modId = "modularrouters";
    public static final String name = "Modular Routers";
    public static final String version = "1.0.0";
    public static final String GUIFACTORY = "me.desht.modularrouters.config.ConfigGuiFactory";

    public static Logger logger;

    private static int modGuiIndex = 0; // track GUI IDs
    public static final int GUI_MODULE = modGuiIndex++;
    public static final int GUI_ROUTER = modGuiIndex++;

    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;

    @SidedProxy(serverSide = "me.desht.modularrouters.proxy.CommonProxy", clientSide = "me.desht.modularrouters.proxy.ClientProxy")
    public static CommonProxy proxy;
    @Mod.Instance(modId)
    public static ModularRouters instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit();
        System.out.println(name + " is loading!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
}
