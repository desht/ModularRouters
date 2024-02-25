package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Received on: CLIENT
 *  <p>
 * Sent when a router GUI is opened to sync all the upgrades to the clientside TE.
 * Various GUI messages/tooltips/etc. depend on knowing what upgrades the router has.
 */
public record RouterUpgradesSyncMessage(BlockPos pos, ItemStackHandler upgradesHandler) implements CustomPacketPayload {
    public static final ResourceLocation ID = MiscUtil.RL("router_upgrades_sync");

    public static RouterUpgradesSyncMessage forRouter(ModularRouterBlockEntity router) {
        BlockPos pos = router.getBlockPos();
        IItemHandler h = router.getUpgrades();
        ItemStackHandler handler = new ItemStackHandler(h.getSlots());
        for (int i = 0; i < h.getSlots(); i++) {
            handler.setStackInSlot(i, h.getStackInSlot(i).copy());
        }
        return new RouterUpgradesSyncMessage(pos, handler);
    }

    public RouterUpgradesSyncMessage(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), createHandler(buf.readNbt()));
    }

    private static ItemStackHandler createHandler(CompoundTag tag) {
        ItemStackHandler h = new ItemStackHandler();
        h.deserializeNBT(tag);
        return h;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(upgradesHandler.serializeNBT());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
