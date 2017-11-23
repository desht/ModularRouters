package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.container.ContainerModule;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class GuiModuleActivator extends GuiModule {
    public GuiModuleActivator(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleActivator(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);
    }
}
