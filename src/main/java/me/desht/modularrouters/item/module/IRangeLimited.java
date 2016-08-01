package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;

interface IRangeLimited {
    ItemSenderModule2.TargetValidation validateTarget(TileEntityItemRouter router, TargetedSender.DimensionPos dimPos);
}
