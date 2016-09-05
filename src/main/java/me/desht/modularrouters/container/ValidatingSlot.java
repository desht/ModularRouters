package me.desht.modularrouters.container;

import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public abstract class ValidatingSlot extends SlotItemHandler {
    private final Class<? extends ItemBase> clazz;

    private ValidatingSlot(Class<? extends ItemBase> clazz, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.clazz = clazz;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack) {
        return clazz.isInstance(itemstack.getItem());
    }

    public static class Module extends ValidatingSlot {
        Module(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(ItemModule.class, itemHandler, index, xPosition, yPosition);
        }
    }

    public static class Upgrade extends ValidatingSlot {
        Upgrade(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(ItemUpgrade.class, itemHandler, index, xPosition, yPosition);
        }
    }
}
