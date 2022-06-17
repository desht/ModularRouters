package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractSmartFilterMenu extends AbstractMRContainerMenu {
    protected final ItemStack filterStack;
    protected final MFLocator locator;
    protected final ModularRouterBlockEntity router;

    AbstractSmartFilterMenu(MenuType<?> type, int windowId, Inventory inv, MFLocator locator) {
        super(type, windowId);

        this.locator = locator;
        this.filterStack = locator.getTargetItem(inv.player);
        this.router = locator.getRouter(inv.player.level).orElse(null);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return router == null || !router.isRemoved();
    }

    public MFLocator getLocator() {
        return locator;
    }

    public ItemStack getFilterStack() {
        return filterStack;
    }

    public ModularRouterBlockEntity getRouter() {
        return router;
    }
}
