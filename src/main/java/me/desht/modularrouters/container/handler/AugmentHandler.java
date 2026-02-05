package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity.RecompileFlag;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class AugmentHandler extends ItemStacksResourceHandler {
    private final ItemStack holderStack;
    private final ModularRouterBlockEntity router;

    public AugmentHandler(ItemStack holderStack, ModularRouterBlockEntity router) {
        super(AugmentItem.SLOTS);
        this.router = router;

        Validate.isTrue(holderStack.getItem() instanceof ModuleItem, "holder stack must be a module!");

        this.holderStack = holderStack;

        var augmentStacks = holderStack.getOrDefault(ModDataComponents.AUGMENTS, ItemContainerContents.EMPTY).stream()
                .limit(AugmentItem.SLOTS)
                .toList();
        for (int i = 0; i < AugmentItem.SLOTS && i < augmentStacks.size(); i++) {
            if (augmentStacks.get(i).getItem() instanceof AugmentItem) {
                setStackInSlot(i, augmentStacks.get(i));
            }
        }
    }

    public ItemStack getHolderStack() {
        return holderStack;
    }

    @Override
    public boolean isValid(int slot, @Nonnull ItemResource resource) {
        if (resource.isEmpty()) return false;
        if (!(resource.getItem() instanceof AugmentItem augment)) return false;

        if (augment.getMaxAugments((ModuleItem) holderStack.getItem()) == 0) return false;

        // can't have the same augment in multiple slots
        for (int i = 0; i < size(); i++) {
            if (slot != i && resource.getItem() == getResource(i).getItem()) return false;
        }

        return true;
    }

    @Override
    protected int getCapacity(int slot, @Nonnull ItemResource resource) {
        if (resource.isEmpty()) {
            return AugmentItem.SLOTS;
        }
        return resource.getItem() instanceof AugmentItem augment ?
                augment.getMaxAugments((ModuleItem) holderStack.getItem()) :
                0;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        save();
    }

    private void save() {
        holderStack.set(ModDataComponents.AUGMENTS, ItemContainerContents.fromItems(stacks));

        if (router != null) {
            router.recompileNeeded(RecompileFlag.MODULES);
        }
    }

    /**
     * Directly overwrites the contents of the handler at a specific index.
     */
    public void setStackInSlot(int slot, ItemStack stack) {
        set(slot, ItemResource.of(stack), stack.getCount());
    }

    /**
     * Get an ItemStack copy of the contents at the given slot.
     */
    public ItemStack getStackInSlot(int slot) {
        return ItemUtil.getStack(this, slot);
    }

    /**
     * Get the number of slots in this handler.
     */
    public int getSlots() {
        return size();
    }
}
