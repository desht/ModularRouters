package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class CompiledVoidModule extends CompiledModule  {
    public CompiledVoidModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(ModularRouterBlockEntity router) {
        ItemStack stack = router.getBufferItemStack();
        if (getFilter().test(stack)) {
            // bye bye items
            int toVoid = Math.min(getItemsPerTick(router), stack.getCount() - getRegulationAmount());
            if (toVoid <= 0) {
                return false;
            }
            ItemResource resource = router.getBuffer().getResource(0);
            if (resource.isEmpty()) {
                return false;
            }
            try (var tx = Transaction.openRoot()) {
                int extracted = router.getBuffer().extract(0, resource, toVoid, tx);
                tx.commit();
                return extracted > 0;
            }
        }
        return false;
    }
}
