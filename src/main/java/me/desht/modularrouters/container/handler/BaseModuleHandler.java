package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public abstract class BaseModuleHandler extends GhostItemHandler {
    private final ItemStack holderStack;
    protected final ModularRouterBlockEntity router;
    private final DataComponentType<ItemContainerContents> componentType;
    private boolean autoSave = true;
    private Consumer<ItemStack> onSaved = stack -> {};

    protected BaseModuleHandler(ItemStack holderStack, ModularRouterBlockEntity router, int size, DataComponentType<ItemContainerContents> componentType) {
        super(size);

        this.holderStack = holderStack;
        this.router = router;
        this.componentType = componentType;

        List<ItemStack> stackList = holderStack.getOrDefault(componentType, ItemContainerContents.EMPTY).nonEmptyItemCopyStream()
                .limit(size)
                .toList();
        for (int i = 0; i < size && i < stackList.size(); i++) {
            setStackInSlot(i, stackList.get(i));
        }
    }

    public void onSaved(Consumer<ItemStack> onSaved) {
        this.onSaved = onSaved;
    }

    /**
     * Run an operation on the handler, deferring any saving till after the whole operation done.
     *
     * @param runnable the operation to run
     */
    public void runBatchOperation(Runnable runnable) {
        autoSave = false;
        runnable.run();
        autoSave = true;
        save();
    }

    /**
     * Get the itemstack which holds this filter.  Could be a module, could be a bulk item filter...
     *
     * @return the holding itemstack
     */
    public ItemStack getHolderStack() {
        return holderStack;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        super.onContentsChanged(index, previousContents);

        if (autoSave) {
            save();
        }
    }

    /**
     * Get the number of items in the filter of the given itemstack.  Counts the items without loading the NBT for
     * every item.
     *
     * @return number of items in the filter
     */
    public static int getFilterItemCount(ItemStack holderStack, BiFunction<ItemStack, ModularRouterBlockEntity, BaseModuleHandler> factory) {
        BaseModuleHandler handler = factory.apply(holderStack, null);
        return (int) IntStream.range(0, handler.getSlots())
                .filter(i -> !handler.getStackInSlot(i).isEmpty())
                .count();
    }

    /**
     * Save the contents of the item handler onto the holder item stack's NBT
     */
    public void save() {
        doSave();

        onSaved.accept(holderStack);
    }

    private void doSave() {
        holderStack.set(componentType, ItemContainerContents.fromItems(stacks));

        if (router != null) {
            router.recompileNeeded(ModularRouterBlockEntity.RecompileFlag.MODULES);
        }
    }

    public static class BulkFilterHandler extends BaseModuleHandler {
        private final ItemStack moduleStack;
        private final int moduleSlot; // when installed in router
        private final int filterSlot;
        private final boolean shouldSave;

        public BulkFilterHandler(ItemStack holderStack, @Nullable ModularRouterBlockEntity router) {
            this(holderStack, router, ItemStack.EMPTY, 0, 0, true);
        }

        public BulkFilterHandler(ItemStack holderStack, ModularRouterBlockEntity router, ItemStack moduleStack, int moduleSlot, int filterSlot, boolean shouldSave) {
            super(holderStack, router, BulkItemFilter.FILTER_SIZE, ModDataComponents.FILTER.get());

            this.moduleStack = moduleStack;
            this.moduleSlot = moduleSlot;
            this.filterSlot = filterSlot;
            this.shouldSave = shouldSave;
        }

        @Override
        public void save() {
            if (shouldSave) {
                super.save();

                if (!moduleStack.isEmpty()) {
                    var h = new ModuleFilterHandler(moduleStack, router);
                    h.setStackInSlot(filterSlot, getHolderStack());
                    h.save();
                    if (router != null) {
                        // since moduleStack is a copy of the actual stack in the router...
                        router.getModules().set(moduleSlot, ItemResource.of(moduleStack), 1);
                    }
                }
            }
        }
    }

    public static class ModuleFilterHandler extends BaseModuleHandler {
        public ModuleFilterHandler(ItemStack holderStack, @Nullable ModularRouterBlockEntity router) {
            super(holderStack, router, Filter.FILTER_SIZE, ModDataComponents.FILTER.get());
        }

        @Override
        public boolean isValid(int index, @Nonnull ItemResource resource) {
            if (resource.isEmpty()) return false;
            return ((ModuleItem) getHolderStack().getItem()).isItemValidForFilter(resource.toStack());
        }
    }
}
