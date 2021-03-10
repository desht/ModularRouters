package me.desht.modularrouters.network;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent when a router GUI is opened to sync all the upgrades to the clientside TE
 * Various GUI messages/tooltips/etc. depend on knowing what upgrades the router has
 */
public class RouterUpgradesSyncMessage {
    private final BlockPos pos;
    private final ItemStackHandler handler;

    public RouterUpgradesSyncMessage(TileEntityItemRouter router) {
        pos = router.getBlockPos();
        IItemHandler upgradesHandler = router.getUpgrades();
        handler = new ItemStackHandler(upgradesHandler.getSlots());
        for (int i = 0; i < upgradesHandler.getSlots(); i++) {
            handler.setStackInSlot(i, upgradesHandler.getStackInSlot(i));
        }
    }

    public RouterUpgradesSyncMessage(PacketBuffer buf) {
        pos = buf.readBlockPos();
        handler = new ItemStackHandler();
        handler.deserializeNBT(buf.readNbt());
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeNbt(handler.serializeNBT());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ClientUtil.theClientWorld();
            if (w != null) {
                TileEntityItemRouter.getRouterAt(w, pos).ifPresent(router -> router.setUpgradesFrom(handler));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
