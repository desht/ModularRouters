package me.desht.modularrouters.container;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.handler.BaseModuleHandler;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nonnull;
import java.util.List;

import static me.desht.modularrouters.container.Layout.SLOT_X_SPACING;

public class Extruder2ModuleMenu extends ModuleMenu {
    private static final int TEMPLATE_SLOTS = 9;

    Extruder2ModuleMenu(int windowId, Inventory inv, FriendlyByteBuf extra) {
        this(windowId, inv, MFLocator.fromNetwork(extra));
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
        } else if (!stackOnCursor.isEmpty() && !ItemStack.isSameItem(stackInSlot, stackOnCursor) && s.mayPlace(stackOnCursor)) {
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
            return true;  //  empty stack is ok, clears the slot
        }
        if (stack.getItem() instanceof BlockItem bi) {
            Block b = bi.getBlock();
            return b.defaultBlockState().getRenderShape() == RenderShape.MODEL
                    && !BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals("chiselsandbits");
        } else {
            return true;  // non-block items are allowed - they act as spacers
        }
    }

    public static class TemplateHandler extends BaseModuleHandler {
        public TemplateHandler(ItemStack holderStack, ModularRouterBlockEntity router) {
            super(holderStack, router, TEMPLATE_SLOTS, ModDataComponents.EXTRUDER2_TEMPLATE.get());
        }

        @Override
        public boolean isValid(int slot, @Nonnull ItemResource resource) {
            return isItemOKForTemplate(resource.toStack());
        }

        public List<ItemStack> toTemplate(int rangeLimit) {
            ImmutableList.Builder<ItemStack> b = new ImmutableList.Builder<>();
            for (int i = 0; i < getSlots() && stacks.size() < rangeLimit; i++) {
                ItemStack stack1 = getStackInSlot(i);
                if (stack1.isEmpty()) {
                    break;
                } else {
                    for (int j = 0; j < stack1.getCount() && stacks.size() < rangeLimit; j++) {
                        b.add(stack1.copyWithCount(1));
                    }
                }
            }
            return b.build();
        }
    }
}
