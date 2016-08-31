package me.desht.modularrouters.block.tile;

import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

abstract class RouterItemHandler extends ItemStackHandler {
    private final Class <? extends ItemBase> clazz;
    private final int flag;
    private final TileEntityItemRouter router;

    private RouterItemHandler(Class<? extends ItemBase> clazz, TileEntityItemRouter router, int flag, int size) {
        super(size);
        this.clazz = clazz;
        this.router = router;
        this.flag = flag;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (clazz.isInstance(stack.getItem())) {
            ItemStack res = super.insertItem(slot, stack, simulate);
            if (res == null || res.stackSize < stack.stackSize) {
                router.recompileNeeded(flag);
            }
            return res;
        } else {
            return stack;
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        router.recompileNeeded(flag);
    }

    static class ModuleHandler extends RouterItemHandler {
        ModuleHandler(TileEntityItemRouter router) {
            super(ItemModule.class, router, TileEntityItemRouter.COMPILE_MODULES, TileEntityItemRouter.N_MODULE_SLOTS);
        }
    }

    static class UpgradeHandler extends RouterItemHandler {
        UpgradeHandler(TileEntityItemRouter router) {
            super(ItemUpgrade.class, router, TileEntityItemRouter.COMPILE_UPGRADES, TileEntityItemRouter.N_UPGRADE_SLOTS);
        }
    }
}
