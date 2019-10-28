package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.client.Minecraft;
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
public class SyncUpgradesMessage {
    private BlockPos pos;
    private ItemStackHandler handler;

    public SyncUpgradesMessage() {
    }

    public SyncUpgradesMessage(TileEntityItemRouter router) {
        pos = router.getPos();
        IItemHandler upgradesHandler = router.getUpgrades();
        handler = new ItemStackHandler(upgradesHandler.getSlots());
        for (int i = 0; i < upgradesHandler.getSlots(); i++) {
            handler.setStackInSlot(i, upgradesHandler.getStackInSlot(i));
        }
    }

    public SyncUpgradesMessage(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pos = pb.readBlockPos();
        handler = new ItemStackHandler();
        handler.deserializeNBT(pb.readCompoundTag());
    }

    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeBlockPos(pos);
        pb.writeCompoundTag(handler.serializeNBT());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World w = ModularRouters.proxy.theClientWorld();
            if (w != null) {
                TileEntityItemRouter.getRouterAt(Minecraft.getInstance().world, pos)
                        .ifPresent(router -> router.setUpgradesFrom(handler));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
