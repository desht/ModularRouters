package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.client.gui.AbstractMRContainerScreen;
import me.desht.modularrouters.client.gui.filter.AbstractFilterContainerScreen;
import me.desht.modularrouters.client.gui.module.AbstractModuleScreen;
import me.desht.modularrouters.container.FilterSlot;
import me.desht.modularrouters.network.ModuleFilterMessage;
import me.desht.modularrouters.network.PacketHandler;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.List;

public class GhostFilterHandler<T extends AbstractMRContainerScreen<?>> implements IGhostIngredientHandler<T> {
    @Override
    public <I> List<Target<I>> getTargets(T gui, I ingredient, boolean doStart) {
        List<Target<I>> res = new ArrayList<>();

        gui.getMenu().slots.forEach(slot -> {
            if (slot instanceof FilterSlot) res.add(new ItemTarget<>(gui, slot));
        });

        return res;
    }

    @Override
    public void onComplete() {
    }

    public static class ModuleFilter extends GhostFilterHandler<AbstractModuleScreen> {
    }

    public static class Filter extends GhostFilterHandler<AbstractFilterContainerScreen> {
    }

    record ItemTarget<I>(AbstractContainerScreen<?> gui, Slot slot) implements Target<I> {
        @Override
        public Rect2i getArea() {
            return new Rect2i(slot.x + gui.getGuiLeft(), slot.y + gui.getGuiTop(), 16, 16);
        }

        @Override
        public void accept(I stack) {
            if (stack instanceof ItemStack) {
                PacketHandler.NETWORK.sendToServer(new ModuleFilterMessage(slot.index, (ItemStack) stack));
            } else if (stack instanceof FluidStack) {
                ItemStack bucket = FluidUtil.getFilledBucket((FluidStack) stack);
                if (!bucket.isEmpty()) {
                    PacketHandler.NETWORK.sendToServer(new ModuleFilterMessage(slot.index, bucket));
                }
            }
        }
    }

}
