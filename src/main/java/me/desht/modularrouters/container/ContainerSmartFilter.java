package me.desht.modularrouters.container;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

public abstract class ContainerSmartFilter extends Container {
    protected final ItemStack filterStack;
    protected final MFLocator locator;
    protected final TileEntityItemRouter router;

    public ContainerSmartFilter(ContainerType<?> type, int windowId, PlayerInventory inv, MFLocator locator) {
        super(type, windowId);

        this.locator = locator;
        this.filterStack = locator.getTargetItem(inv.player);
        this.router = locator.getRouter(inv.player.world).isPresent() ? locator.getRouter(inv.player.world).get() : null;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    public MFLocator getLocator() {
        return locator;
    }

    public ItemStack getFilterStack() {
        return filterStack;
    }

    public TileEntityItemRouter getRouter() {
        return router;
    }
}
