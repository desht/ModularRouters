package me.desht.modularrouters.container.slot;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public abstract class ValidatingSlot extends SlotItemHandler {
    private final Class<? extends ItemBase> clazz;
    protected final TileEntityItemRouter router;

    private ValidatingSlot(Class<? extends ItemBase> clazz, TileEntityItemRouter router, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.clazz = clazz;
        this.router = router;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack) {
        return clazz.isInstance(itemstack.getItem()) && super.isItemValid(itemstack);
    }

    public static class Module extends ValidatingSlot {
        public Module(TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(ItemModule.class, router, router.getModules(), index, xPosition, yPosition);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
        }
    }

    public static class Upgrade extends ValidatingSlot {
        public Upgrade(TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(ItemUpgrade.class, router, router.getUpgrades(), index, xPosition, yPosition);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            router.recompileNeeded(TileEntityItemRouter.COMPILE_UPGRADES);
        }
    }
}
