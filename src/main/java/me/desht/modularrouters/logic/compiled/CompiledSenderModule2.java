package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.world.item.ItemStack;

public class CompiledSenderModule2 extends CompiledSenderModule1 {
    public CompiledSenderModule2(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected PositionedItemHandler findTargetInventory(ModularRouterBlockEntity router) {
        ModuleTarget target = getEffectiveTarget(router);
        if (target == null || !isTargetValid(router, target)) {
            return PositionedItemHandler.INVALID;
        }

        return target.getItemHandler().map(h -> new PositionedItemHandler(target.gPos.pos(), h))
                .orElse(PositionedItemHandler.INVALID);
    }

    @Override
    protected int getBeamColor() {
        return 0xFF8000;
    }

    @Override
    public ModuleTarget getEffectiveTarget(ModularRouterBlockEntity router) {
        return getTarget();
    }
}
