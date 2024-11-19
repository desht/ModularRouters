package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BufferHandler extends ItemStackHandler {
    // we mask null keys in the cap cache map with this object to avoid needing to check containsKey
    private static final Object NULL = new Object();

    private final ModularRouterBlockEntity router;

    // default expected size of 2 is reasonable for most routers
    private final IdentityHashMap<ItemCapability<?, Void>, Object> capabilityCache = new IdentityHashMap<>(4);

    public BufferHandler(ModularRouterBlockEntity router) {
        super(router.getBufferSlotCount());
        this.router = router;
    }

    @Override
    public void onContentsChanged(int slot) {
        // small optimisation in case we don't have any caps requested already
        if (!capabilityCache.isEmpty()) {
            ItemStack stack = getStackInSlot(slot);

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

    private <T> T revalidate(ItemStack stack, ItemCapability<T, Void> cap, T old, AtomicBoolean modified) {
        var newVal = maskNull(stack.getCapability(cap));
        if (newVal != old) {
            modified.compareAndSet(false, true);
        }
        return newVal;
    }

    public IFluidHandlerItem getFluidHandler() {
        return getCapability(Capabilities.FluidHandler.ITEM);
    }

    public IEnergyStorage getEnergyStorage() {
        return getCapability(Capabilities.EnergyStorage.ITEM);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, Void> cap) {
        var cached = capabilityCache.get(cap);
        if (cached == null) {
            cached = maskNull(getStackInSlot(0).getCapability(cap));
            capabilityCache.put(cap, cached);
        }
        return cached == NULL ? null : (T)cached;
    }

    @SuppressWarnings("unchecked")
    private static <T> T maskNull(T value) {
        return value == null ? (T) NULL : value;
    }
}
