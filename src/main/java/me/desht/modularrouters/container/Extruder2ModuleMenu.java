package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

import javax.annotation.Nonnull;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;

public class Extruder2ModuleMenu extends ModuleMenu {
    private static final int TEMPLATE_SLOTS = 9;

    Extruder2ModuleMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        this(windowId, inv, MFLocator.fromBuffer(extra));
    }

    public Extruder2ModuleMenu(int windowId, Inventory inv, MFLocator locator) {
        super(ModMenuTypes.EXTRUDER2_MENU.get(), windowId, inv, locator);

        TemplateHandler handler = new TemplateHandler(locator.getModuleStack(inv.player), router);
        for (int i = 0; i < TEMPLATE_SLOTS; i++) {
            addSlot(new FilterSlot(handler, i, 129 + SLOT_X_SPACING * (i % 3), 17 + SLOT_X_SPACING * (i / 3)));
        }
    }

    @Override
    protected void transferStackInExtraSlot(Player player, int index) {
        slots.get(index).set(ItemStack.EMPTY);
    }

    @Override
    protected void slotClickExtraSlot(int slot, int dragType, ClickType clickTypeIn, Player player) {
        Slot s = slots.get(slot);
        ItemStack stackOnCursor = getCarried();
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
    }

    private static boolean isItemOKForTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;  //  null is ok, clears the slot
        }
        if (stack.getItem() instanceof BlockItem bi) {
            Block b = bi.getBlock();
            return b.defaultBlockState().getRenderShape() == RenderShape.MODEL
                    && !MiscUtil.getRegistryName(b).orElseThrow().getNamespace().equals("chiselsandbits");
        } else {
            return true;  // non-block items are allowed - they act as spacers
        }
    }

    public static class TemplateHandler extends BaseModuleHandler {
        private static final String NBT_TEMPLATE = "Template";

        public TemplateHandler(ItemStack holderStack, ModularRouterBlockEntity router) {
            super(holderStack, router, TEMPLATE_SLOTS, NBT_TEMPLATE);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return isItemOKForTemplate(stack);
        }
    }
}
