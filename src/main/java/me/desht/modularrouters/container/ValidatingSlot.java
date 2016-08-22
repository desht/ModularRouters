package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public abstract class ValidatingSlot extends SlotItemHandler {
    private final Class<? extends ItemBase> clazz;
    private final TileEntityItemRouter router;

    private ValidatingSlot(Class<? extends ItemBase> clazz, TileEntityItemRouter router, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.clazz = clazz;
        this.router = router;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        router.recompileNeeded(compileWhat());
    }

    abstract int compileWhat();

    @Override
    public boolean isItemValid(ItemStack itemstack) {
        return clazz.isInstance(itemstack.getItem());
    }

    public static class Module extends ValidatingSlot {
        Module(TileEntityItemRouter router, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(ItemModule.class, router, itemHandler, index, xPosition, yPosition);
        }

        @Override
        int compileWhat() {
            return TileEntityItemRouter.COMPILE_MODULES;
        }
    }

    public static class Upgrade extends ValidatingSlot {
        Upgrade(TileEntityItemRouter router, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(ItemUpgrade.class, router, itemHandler, index, xPosition, yPosition);
        }

        @Override
        int compileWhat() {
            return TileEntityItemRouter.COMPILE_UPGRADES;
        }
    }
}
