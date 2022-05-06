package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.client.gui.module.AbstractModuleScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.RL;

@JeiPlugin
public class JEIModularRoutersPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return RL("default");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(AbstractModuleScreen.class, new GuiModuleGhost());

        registration.addGuiContainerHandler(ModularRouterScreen.class, new IGuiContainerHandler<>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(ModularRouterScreen routerScreen) {
                return routerScreen.getExtraArea();
            }
        });
    }
}
