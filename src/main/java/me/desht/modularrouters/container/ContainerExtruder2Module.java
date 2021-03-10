package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;

public class ContainerExtruder2Module extends ContainerModule {
    private static final int TEMPLATE_SLOTS = 9;

    ContainerExtruder2Module(int windowId, PlayerInventory inv, PacketBuffer extra) {
        this(windowId, inv, MFLocator.fromBuffer(extra));
    }

    public ContainerExtruder2Module(int windowId, PlayerInventory inv, MFLocator locator) {
        super(ModContainerTypes.CONTAINER_MODULE_EXTRUDER2.get(), windowId, inv, locator);

        TemplateHandler handler = new TemplateHandler(locator.getModuleStack(inv.player), router);
        for (int i = 0; i < TEMPLATE_SLOTS; i++) {
            addSlot(new FilterSlot(handler, i, 129 + SLOT_X_SPACING * (i % 3), 17 + SLOT_X_SPACING * (i / 3)));
        }
    }

    @Override
    protected void transferStackInExtraSlot(PlayerEntity player, int index) {
        slots.get(index).set(ItemStack.EMPTY);
    }

    @Override
    protected ItemStack slotClickExtraSlot(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        Slot s = slots.get(slot);
        ItemStack stackOnCursor = player.inventory.getCarried();
        ItemStack stackInSlot = s.getItem().copy();
        if (clickTypeIn == ClickType.QUICK_MOVE) {
            s.set(ItemStack.EMPTY);  // shift-left-click clears the slot
        } else if (!stackOnCursor.isEmpty() && !ItemStack.isSame(stackInSlot, stackOnCursor) && s.mayPlace(stackOnCursor)) {
            // placing a new item in the template buffer
            ItemStack stack1 = stackOnCursor.copy();
            if (dragType == 1) {
                stack1.setCount(1);
            }
            s.set(stack1);
        } else {
            if (!stackInSlot.isEmpty()) {
                if (dragType == 1) {
                    // right-click increments the stack size
                    stackInSlot.setCount(Math.min(stackInSlot.getMaxStackSize(), stackInSlot.getCount() + 1));
                } else if (dragType == 0) {
                    // left-click decrements the stack size
                    stackInSlot.shrink(1);
                }
                s.set(stackInSlot);
                s.setChanged();  // need explicit call here
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isItemOKForTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;  //  null is ok, clears the slot
        }
        if (!(stack.getItem() instanceof BlockItem)) {
            return true;  // non-block items are allowed - they act as spacers
        }
        Block b = ((BlockItem) stack.getItem()).getBlock();
        return b.defaultBlockState().getRenderShape() == BlockRenderType.MODEL && !b.getRegistryName().getNamespace().equals("chiselsandbits");
    }

    public static class TemplateHandler extends BaseModuleHandler {
        private static final String NBT_TEMPLATE = "Template";

        public TemplateHandler(ItemStack holderStack, TileEntityItemRouter router) {
            super(holderStack, router, TEMPLATE_SLOTS, NBT_TEMPLATE);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return isItemOKForTemplate(stack);
        }
    }
}
