package me.desht.modularrouters.container;

import me.desht.modularrouters.core.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class ModuleMenuFactories {
    public static ModuleMenu createActivatorMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.ACTIVATOR_MENU.get(), windowId, inv, extra);
    }

    public static ModuleMenu createBreakerMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.BREAKER_MENU.get(), windowId, inv, extra);
    }

    public static ModuleMenu createDetectorMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.DETECTOR_MENU.get(), windowId, inv, extra);
    }

    public static ModuleMenu createDistributorMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.DISTRIBUTOR_MENU.get(), windowId, inv, extra);
    }

    public static Extruder2ModuleMenu createExtruder2Menu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new Extruder2ModuleMenu(windowId, inv, extra);
    }

    public static ModuleMenu createFlingerMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.FLINGER_MENU.get(), windowId, inv, extra);
    }

    public static ModuleMenu createFluidMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.FLUID_MENU.get(), windowId, inv, extra);
    }


    public static ModuleMenu createGasMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.GAS_MENU.get(), windowId, inv, extra);
    }



    public static ModuleMenu createPlayerMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.PLAYER_MENU.get(), windowId, inv, extra);
    }

    public static ModuleMenu createVacuumMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        return new ModuleMenu(ModMenuTypes.VACUUM_MENU.get(), windowId, inv, extra);
    }
}
