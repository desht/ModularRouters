package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BufferHandler extends ItemStacksResourceHandler {
    // we mask null keys in the cap cache map with this object to avoid needing to check containsKey
    private static final Object NULL = new Object();

    private final ModularRouterBlockEntity router;

    // default expected size of 2 is reasonable for most routers
    private final IdentityHashMap<ItemCapability<?, ItemAccess>, Object> capabilityCache = new IdentityHashMap<>(4);

    public BufferHandler(ModularRouterBlockEntity router) {
        super(router.getBufferSlotCount());
        this.router = router;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        // small optimisation in case we don't have any caps requested already
        if (!capabilityCache.isEmpty()) {
            ItemStack stack = ItemUtil.getStack(this, index);

            var modified = new AtomicBoolean();

            // replace and invalidate if necessary the capabilities we previously returned
            //noinspection rawtypes,unchecked
            capabilityCache.replaceAll((cap, old) -> revalidate(stack, (ItemCapability) cap, old, modified));

            if (modified.get()) {
                router.invalidateCapabilities();

                // in case any pipes/cables need to connect/disconnect
                router.nonNullLevel().updateNeighborsAt(router.getBlockPos(), ModBlocks.MODULAR_ROUTER.get());
            }
        }

        router.setChanged();  // will also update comparator output
    }

    private <T> T revalidate(ItemStack stack, ItemCapability<T, ItemAccess> cap, T old, AtomicBoolean modified) {
        T newVal;
        if (stack.isEmpty()) {
            newVal = maskNull(null);
        } else {
            newVal = maskNull(stack.getCapability(cap, ItemAccess.forHandlerIndex(this, 0)));
        }
        if (newVal != old) {
            modified.compareAndSet(false, true);
        }
        return newVal;
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler() {
        return getCapability(Capabilities.Fluid.ITEM);
    }

    @Nullable
    public EnergyHandler getEnergyStorage() {
        return getCapability(Capabilities.Energy.ITEM);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, ItemAccess> cap) {
        var cached = capabilityCache.get(cap);
        if (cached == null) {
            ItemStack stack = ItemUtil.getStack(this, 0);
            if (stack.isEmpty()) {
                return null;
            }
            cached = maskNull(stack.getCapability(cap, ItemAccess.forHandlerIndex(this, 0)));
            capabilityCache.put(cap, cached);
        }
        return cached == NULL ? null : (T)cached;
    }

    @SuppressWarnings("unchecked")
    private static <T> T maskNull(T value) {
        return value == null ? (T) NULL : value;
    }

    /**
     * Directly overwrites the contents of the handler at a specific index.
     * Used for slot synchronization.
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
}
