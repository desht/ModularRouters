package me.desht.modularrouters.container.handler;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class AugmentHandler extends BaseModuleHandler {
    public AugmentHandler(ItemStack holderStack, @Nullable ModularRouterBlockEntity router) {
        super(holderStack, router, AugmentItem.SLOTS, ModDataComponents.AUGMENTS.get());
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        if (!(resource.getItem() instanceof AugmentItem augment)) return false;

        if (augment.getMaxAugments((ModuleItem) getHolderStack().getItem()) == 0) return false;

        // can't have the same augment in multiple slots
        for (int i = 0; i < size(); i++) {
            if (slot != i && resource.getItem() == getResource(i).getItem()) return false;
        }

        return true;
    }

    @Override
    protected int getCapacity(int slot, ItemResource resource) {
        if (resource.isEmpty()) {
            return AugmentItem.SLOTS;
        }
        return resource.getItem() instanceof AugmentItem augment ?
                augment.getMaxAugments((ModuleItem) getHolderStack().getItem()) :
                0;
    }
}
