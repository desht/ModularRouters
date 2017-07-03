package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.item.augment.Augment;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class AugmentHandler extends ItemStackHandler {
    private final ItemStack holderStack;

    public AugmentHandler(ItemStack holderStack) {
        super(Augment.SLOTS);

        this.holderStack = holderStack;
        if (!holderStack.hasTagCompound()) {
            holderStack.setTagCompound(new NBTTagCompound());
        }
        deserializeNBT(holderStack.getTagCompound().getCompoundTag(ModuleHelper.NBT_AUGMENTS));
    }

    public ItemStack getHolderStack() {
        return holderStack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack.getItem() instanceof ItemAugment ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (stack.getItem() instanceof ItemAugment) {
            super.setStackInSlot(slot, stack);
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        save();
    }

    public void save() {
        holderStack.getTagCompound().setTag(ModuleHelper.NBT_AUGMENTS, serializeNBT());
    }
}
