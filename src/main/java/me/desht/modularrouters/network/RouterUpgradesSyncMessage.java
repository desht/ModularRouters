package me.desht.modularrouters.network;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

/**
 * Received on: CLIENT
 * Sent when a router GUI is opened to sync all the upgrades to the clientside TE
 * Various GUI messages/tooltips/etc. depend on knowing what upgrades the router has
 */
public class RouterUpgradesSyncMessage implements SimpleMessage {
    private final BlockPos pos;
    private final ItemStackHandler handler;

    public RouterUpgradesSyncMessage(ModularRouterBlockEntity router) {
        pos = router.getBlockPos();
        IItemHandler upgradesHandler = router.getUpgrades();
        handler = new ItemStackHandler(upgradesHandler.getSlots());
        for (int i = 0; i < upgradesHandler.getSlots(); i++) {
            handler.setStackInSlot(i, upgradesHandler.getStackInSlot(i));
        }
    }

    public RouterUpgradesSyncMessage(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        handler = new ItemStackHandler();
        handler.deserializeNBT(buf.readNbt());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(handler.serializeNBT());
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        Level level = ClientUtil.theClientLevel();
        if (level != null && level.isLoaded(pos)) {
            level.getBlockEntity(pos, ModBlockEntities.MODULAR_ROUTER.get())
                    .ifPresent(router -> router.setUpgradesFrom(handler));
        }
    }
}
