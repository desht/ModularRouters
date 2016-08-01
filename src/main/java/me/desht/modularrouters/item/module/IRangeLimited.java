package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;

interface IRangeLimited {
    boolean isValidTarget(TileEntityItemRouter router, TargetedSender.DimensionPos dimPos);
}
