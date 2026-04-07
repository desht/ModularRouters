package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class CompiledSenderModule2 extends CompiledSenderModule1 {
    public CompiledSenderModule2(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    protected Optional<PositionedItemHandler> findTargetInventory(ModularRouterBlockEntity router) {
        var val = getEffectiveTarget(router);
        if (val.isPresent()) {
            ModuleTarget target = val.get();
            if (isTargetValid(router, val.get())) {
                return target.getItemHandler().map(h -> new PositionedItemHandler(target.gPos.pos(), h));
            }
        }

        return Optional.empty();
    }

    @Override
    protected int getBeamColor() {
        return 0xFF8000;
    }

    @Override
    public Optional<ModuleTarget> getEffectiveTarget(ModularRouterBlockEntity router) {
        return getTarget();
    }
}
