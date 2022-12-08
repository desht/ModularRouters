package me.desht.modularrouters.integration.jei;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

record GhostTarget<I>(AbstractContainerScreen<?> gui, Slot slot) /*implements IGhostIngredientHandler.Target<I>*/ {
//    @Override
//    public Rect2i getArea() {
//        return new Rect2i(slot.x + gui.getGuiLeft(), slot.y + gui.getGuiTop(), 16, 16);
//    }
//
//    @Override
//    public void accept(I ingredient) {
//        if (ingredient instanceof ItemStack stack) {
//            PacketHandler.NETWORK.sendToServer(new ModuleFilterMessage(slot.index, stack));
//        } else if (ingredient instanceof FluidStack fluidStack) {
//            ItemStack bucket = FluidUtil.getFilledBucket(fluidStack);
//            if (!bucket.isEmpty()) {
//                PacketHandler.NETWORK.sendToServer(new ModuleFilterMessage(slot.index, bucket));
//            }
//        }
//    }
}
