package me.desht.modularrouters.logic;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.item.ItemStack;

public class CompiledModule {
    private final Filter filter;
    private final Module module;
    private final Module.RelativeDirection direction;
    private final RouterTarget target;
    private final boolean termination;

    public CompiledModule(TileEntityItemRouter router, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModule)) {
            throw new IllegalArgumentException("expected module router module, got " + stack);
        }

        filter = new Filter(stack);
        module = ItemModule.getModule(stack);
        direction = module.getDirectionFromNBT(stack);
        termination = module.terminates(stack);
        target = module.getTarget(router, stack);
    }

    public Module getModule() {
        return module;
    }

    public Filter getFilter() {
        return filter;
    }

    public Module.RelativeDirection getDirection() {
        return direction;
    }

    public boolean execute(TileEntityItemRouter router) {
        return module.execute(router, this);
    }

    public RouterTarget getTarget() {
        return target;
    }

    public boolean termination() {
        return termination;
    }

}
