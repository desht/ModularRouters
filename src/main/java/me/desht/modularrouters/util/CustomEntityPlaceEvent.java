package me.desht.modularrouters.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import org.antlr.v4.runtime.misc.NotNull;

import javax.annotation.Nullable;

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

