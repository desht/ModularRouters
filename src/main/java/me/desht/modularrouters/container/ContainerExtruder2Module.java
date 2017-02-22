package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.container.slot.BaseModuleSlot;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;

public class ContainerExtruder2Module extends ContainerModule {
    private static final int TEMPLATE_SLOTS = 9;

    public ContainerExtruder2Module(EntityPlayer player, EnumHand hand, ItemStack moduleStack) {
        this(player, hand, moduleStack, null);
    }

    public ContainerExtruder2Module(EntityPlayer player, EnumHand hand, ItemStack moduleStack, TileEntityItemRouter router) {
        super(player, hand, moduleStack, router);

        TemplateHandler handler = new TemplateHandler(moduleStack);
        for (int i = 0; i < TEMPLATE_SLOTS; i++) {
            int x = 129 + SLOT_X_SPACING * (i % 3);
            int y = 17 + SLOT_X_SPACING * (i / 3);
            TemplateSlot slot = router == null ?
                    new TemplateSlot(handler, player, hand, i, x, y) :
                    new TemplateSlot(handler, router, i, x, y);
            addSlotToContainer(slot);
        }
    }

    @Override
    protected void transferStackInExtraSlot(EntityPlayer player, int index) {
        inventorySlots.get(index).putStack(null);
    }

    @Override
    protected ItemStack slotClickExtraSlot(int slot, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        Slot s = inventorySlots.get(slot);
        ItemStack stackOnCursor = player.inventory.getItemStack();
        if (!stackOnCursor.isEmpty()) {
            // placing a new item in the template buffer
            ItemStack stack1 = stackOnCursor.copy();
            stack1.setCount(1);
            s.putStack(stack1);
        } else if (clickTypeIn == ClickType.QUICK_MOVE) {
            s.putStack(null);  // shift-left-click clears the slot
        } else {
            ItemStack stack = s.getStack();
            if (!stack.isEmpty()) {
                if (dragType == 1) {
                    // right-click increments the stack size
                    stack.grow(1);
                } else if (dragType == 0) {
                    // left-click decrements the stack size
                    stack.shrink(1);
                }
                s.putStack(stack);
                s.onSlotChanged();  // need explicit call here
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isItemOKForTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;  //  null is ok, clears the slot
        }
        if (!(stack.getItem() instanceof ItemBlock)) {
            return false;
        }
        Block b = ((ItemBlock) stack.getItem()).getBlock();
        return b.getDefaultState().getRenderType() == EnumBlockRenderType.MODEL;
    }

    private static class TemplateSlot extends BaseModuleSlot<TemplateHandler> {
        public TemplateSlot(TemplateHandler itemHandler, TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(itemHandler, router, index, xPosition, yPosition);
        }

        public TemplateSlot(TemplateHandler itemHandler, EntityPlayer player, EnumHand hand, int index, int xPosition, int yPosition) {
            super(itemHandler, player, hand, index, xPosition, yPosition);
        }
    }

    public static class TemplateHandler extends BaseModuleHandler {
        private static final String NBT_TEMPLATE = "Template";

        public TemplateHandler(ItemStack holderStack) {
            super(holderStack, TEMPLATE_SLOTS, NBT_TEMPLATE);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isItemOKForTemplate(stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (isItemOKForTemplate(stack)) {
                super.setStackInSlot(slot, stack);
            }
        }
    }
}
