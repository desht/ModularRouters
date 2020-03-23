package me.desht.modularrouters.container;

import me.desht.modularrouters.core.ModContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ContainerModules {
    public static ContainerModule createActivatorContainer(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerModule(ModContainerTypes.CONTAINER_MODULE_ACTIVATOR.get(), windowId, inv, extra);
    }

    public static ContainerModule createDetectorContainer(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerModule(ModContainerTypes.CONTAINER_MODULE_DETECTOR.get(), windowId, inv, extra);
    }

    public static ContainerModule createDistributorContainer(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerModule(ModContainerTypes.CONTAINER_MODULE_DETECTOR.get(), windowId, inv, extra);
    }

    public static ContainerExtruder2Module createExtruder2Container(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerExtruder2Module(windowId, inv, extra);
    }

    public static ContainerModule createFlingerContainer(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerModule(ModContainerTypes.CONTAINER_MODULE_FLINGER.get(), windowId, inv, extra);
    }

    public static ContainerModule createFluidContainer(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerModule(ModContainerTypes.CONTAINER_MODULE_FLUID.get(), windowId, inv, extra);
    }

    public static ContainerModule createPlayerContainer(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerModule(ModContainerTypes.CONTAINER_MODULE_PLAYER.get(), windowId, inv, extra);
    }

    public static ContainerModule createVacuumContainer(int windowId, PlayerInventory inv, PacketBuffer extra) {
        return new ContainerModule(ModContainerTypes.CONTAINER_MODULE_VACUUM.get(), windowId, inv, extra);
    }
}
