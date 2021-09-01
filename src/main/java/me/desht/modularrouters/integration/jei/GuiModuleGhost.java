package me.desht.modularrouters.integration.jei;

import me.desht.modularrouters.client.gui.module.AbstractModuleScreen;
import me.desht.modularrouters.container.FilterSlot;
import me.desht.modularrouters.network.ModuleFilterMessage;
import me.desht.modularrouters.network.PacketHandler;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.List;

public class GuiModuleGhost implements IGhostIngredientHandler<AbstractModuleScreen> {
    @Override
    public <I> List<Target<I>> getTargets(AbstractModuleScreen gui, I ingredient, boolean doStart) {
        List<Target<I>> res = new ArrayList<>();
        for (int i = 0; i < gui.getMenu().slots.size(); i++) {
            Slot s = gui.getMenu().getSlot(i);
            if (s instanceof FilterSlot) {
                res.add(new ItemTarget<>(gui, s));
            }
        }
        return res;
    }

    @Override
    public void onComplete() {
    }

    static class ItemTarget<I> implements Target<I> {
        private final AbstractModuleScreen gui;
        private final Slot slot;

        ItemTarget(AbstractModuleScreen gui, Slot slot) {
            this.gui = gui;
            this.slot = slot;
        }

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
