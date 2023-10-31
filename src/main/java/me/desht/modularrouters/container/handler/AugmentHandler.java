package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class AugmentHandler extends ItemStackHandler {
    private final ItemStack holderStack;
    private final ModularRouterBlockEntity router;

    public AugmentHandler(ItemStack holderStack, ModularRouterBlockEntity router) {
        super(AugmentItem.SLOTS);
        this.router = router;

        Validate.isTrue(holderStack.getItem() instanceof ModuleItem, "holder stack must be a module!");

        this.holderStack = holderStack;
        deserializeNBT(holderStack.getOrCreateTagElement(ModularRouters.MODID).getCompound(ModuleHelper.NBT_AUGMENTS));
    }

    public ItemStack getHolderStack() {
        return holderStack;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (!(stack.getItem() instanceof AugmentItem augment)) return false;

        if (augment.getMaxAugments((ModuleItem) holderStack.getItem()) == 0) return false;

        // can't have the same augment in multiple slots
        for (int i = 0; i < getSlots(); i++) {
            if (slot != i && stack.getItem() == getStackInSlot(i).getItem()) return false;
        }

        return true;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        if (stack.getItem() instanceof AugmentItem augment) {
            return augment.getMaxAugments((ModuleItem) holderStack.getItem());
        }
        return 0;
    }

    @Override
    protected void onContentsChanged(int slot) {
        save();
    }

    private void save() {
        holderStack.getOrCreateTagElement(ModularRouters.MODID).put(ModuleHelper.NBT_AUGMENTS, serializeNBT());
        if (router != null) {
            router.recompileNeeded(ModularRouterBlockEntity.COMPILE_MODULES);
        }
    }
}
