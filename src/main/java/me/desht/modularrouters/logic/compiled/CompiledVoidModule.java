package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CompiledVoidModule extends CompiledModule  {
    public CompiledVoidModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        ItemStack stack = router.getBufferItemStack();
        if (getFilter().test(stack)) {
            // bye bye items
            int toVoid = Math.min(getItemsPerTick(router), stack.getCount() - getRegulationAmount());
            if (toVoid <= 0) {
                return false;
            }
            ItemStack gone = router.getBuffer().extractItem(0, toVoid, false);
            return !gone.isEmpty();
        }
        return false;
    }
}
