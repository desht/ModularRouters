package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.module.GuiModuleActivator;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ActivatorModule extends Module {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledActivatorModule(router, stack);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleActivator.class;
    }

    @Override
    public Color getItemTint() {
        return new Color(224, 224, 224);
    }
}
