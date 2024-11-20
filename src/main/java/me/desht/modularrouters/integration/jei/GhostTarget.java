package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.network.messages.ModuleFilterMessage;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

record GhostTarget<I>(IIngredientType<I> type, AbstractContainerScreen<?> gui, Slot slot) implements IGhostIngredientHandler.Target<I> {
    @Override
    public Rect2i getArea() {
        return new Rect2i(slot.x + gui.getGuiLeft(), slot.y + gui.getGuiTop(), 16, 16);
    }

    @Override
    public void accept(I ingredient) {
        var creator = JEIModularRoutersPlugin.STACK_CREATORS.get(type);
        if (creator != null) {
            var stack = creator.apply(ingredient);
            if (!stack.isEmpty()) {
                PacketDistributor.sendToServer(new ModuleFilterMessage(slot.index, stack));
            }
        }
    }
}
