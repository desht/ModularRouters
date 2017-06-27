package me.desht.modularrouters;

import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import me.desht.modularrouters.gui.GuiHandler;
import me.desht.modularrouters.integration.IntegrationHandler;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.upgrade.CamouflageUpgrade;
import me.desht.modularrouters.item.upgrade.SecurityUpgrade;
import me.desht.modularrouters.network.*;
import me.desht.modularrouters.proxy.CommonProxy;
import me.desht.modularrouters.recipe.ModRecipes;
import me.desht.modularrouters.sound.ModSounds;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModularRouters.MODID, version = ModularRouters.MODVERSION, name = ModularRouters.MODNAME,
        dependencies = ModularRouters.DEPENDENICES,
        acceptedMinecraftVersions = "1.12"
)
public class ModularRouters {
    public static final String MODID = "modularrouters";
    public static final String MODNAME = "Modular Routers";
    public static final String MODVERSION = "@VERSION@";
    public static final String DEPENDENICES =
            "after:Waila;before:guideapi@[1.12-2.1.3-55,);after:theoneprobe;"
                    + "required-after:forge@[14.21.0.2368,);";

    public static Logger logger;
    public static SimpleNetworkWrapper network;

    private static int modGuiIndex = 0; // track GUI IDs
    public static final int GUI_MODULE_HELD_MAIN = modGuiIndex++;
    public static final int GUI_MODULE_HELD_OFF = modGuiIndex++;
    public static final int GUI_ROUTER = modGuiIndex++;
    public static final int GUI_MODULE_INSTALLED = modGuiIndex++;
    public static final int GUI_FILTER_HELD_MAIN = modGuiIndex++;
    public static final int GUI_FILTER_HELD_OFF = modGuiIndex++;
    public static final int GUI_FILTER_INSTALLED = modGuiIndex++;
    public static final int GUI_SYNC_UPGRADE = modGuiIndex++;

    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;

    @SidedProxy(serverSide = "me.desht.modularrouters.proxy.CommonProxy", clientSide = "me.desht.modularrouters.proxy.ClientProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static ModularRouters instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        ModItems.init();
        ModBlocks.init();
        ModSounds.init();
        proxy.preInit();
        setupNetwork();
        GameRegistry.registerTileEntity(TileEntityItemRouter.class, "item_router");
        GameRegistry.registerTileEntity(TileEntityTemplateFrame.class, "template_frame");
        logger.info(MODNAME + " is loading!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        ModRecipes.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(ModularRouters.instance, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(SecurityUpgrade.Interacted.class);
        MinecraftForge.EVENT_BUS.register(CamouflageUpgrade.Interacted.class);
        MinecraftForge.EVENT_BUS.register(BlockItemRouter.ExplosionHandler.class);
        IntegrationHandler.registerAll();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        IntegrationHandler.checkForXpJuice();
        ModNameCache.init();
    }

    private void setupNetwork() {
        int d = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel(ModularRouters.MODID);
        network.registerMessage(RouterSettingsMessage.Handler.class, RouterSettingsMessage.class, d++, Side.SERVER);
        network.registerMessage(ModuleSettingsMessage.Handler.class, ModuleSettingsMessage.class, d++, Side.SERVER);
        network.registerMessage(FilterSettingsMessage.Handler.class, FilterSettingsMessage.class, d++, Side.SERVER);
        network.registerMessage(OpenGuiMessage.Handler.class, OpenGuiMessage.class, d++, Side.SERVER);
        network.registerMessage(ParticleBeamMessage.Handler.class, ParticleBeamMessage.class, d++, Side.CLIENT);
        network.registerMessage(GuiSyncMessage.Handler.class, GuiSyncMessage.class, d++, Side.CLIENT);
        network.registerMessage(SyncUpgradeSettingsMessage.Handler.class, SyncUpgradeSettingsMessage.class, d++, Side.SERVER);
    }
}
