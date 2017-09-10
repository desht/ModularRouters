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
    private final int recompile;
    protected final TileEntityItemRouter router;

    private ValidatingSlot(Class<? extends ItemBase> clazz, int recompile, TileEntityItemRouter router, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.clazz = clazz;
        this.recompile = recompile;
        this.router = router;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack) {
        return clazz.isInstance(itemstack.getItem()) && super.isItemValid(itemstack);
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        router.recompileNeeded(recompile);
    }

    public static class Module extends ValidatingSlot {
        public Module(TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(ItemModule.class, TileEntityItemRouter.COMPILE_MODULES, router, router.getModules(), index, xPosition, yPosition);
        }
    }

    public static class Upgrade extends ValidatingSlot {
        public Upgrade(TileEntityItemRouter router, int index, int xPosition, int yPosition) {
            super(ItemUpgrade.class, TileEntityItemRouter.COMPILE_UPGRADES, router, router.getUpgrades(), index, xPosition, yPosition);
        }
    }
}
