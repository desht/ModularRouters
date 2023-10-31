package me.desht.modularrouters.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Workaround for a problem in the Forge EntityPlaceEvent where the new blockstate isn't available in the event
 * because it is init'd in the event constructor from the block snapshot, which doesn't yet have the new blockstate.
 */
public class CustomEntityPlaceEvent extends BlockEvent.EntityPlaceEvent {
    private final BlockState newState;

    public CustomEntityPlaceEvent(@NotNull BlockSnapshot blockSnapshot, @NotNull BlockState placedAgainst, @Nullable Entity entity, @NotNull BlockState newState) {
        super(blockSnapshot, placedAgainst, entity);
        this.newState = newState;
    }

    @Override
    public BlockState getPlacedBlock() {
        return newState;
    }
}
