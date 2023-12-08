package me.desht.modularrouters.network;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleMessage;

import java.util.Optional;

/**
 * Received on: SERVER
 *
 * Sent when a filter's settings have been changed in any way via its GUI.
 * The filter could be in a player's hand, or in a module (which may or may not be in a router...)
 */
public class FilterSettingsMessage implements SimpleMessage {
    private final Operation op;
    private final CompoundTag payload;
    private final MFLocator locator;

    public FilterSettingsMessage(Operation op, MFLocator locator, CompoundTag payload) {
        this.op = op;
        this.locator = locator;
        this.payload = payload;
    }

    public FilterSettingsMessage(FriendlyByteBuf buf) {
        op = buf.readEnum(Operation.class);
        locator = MFLocator.fromBuffer(buf);
        payload = buf.readNbt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(op);
        locator.writeBuf(buffer);
        buffer.writeNbt(payload);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            processPacket(player);
        }
    }

    public Operation getOp() {
        return op;
    }

    public CompoundTag getPayload() {
        return payload;
    }

    public Optional<IItemHandler> getTargetInventory() {
        ModuleTarget target = ModuleTarget.fromNBT(payload);
        return target.getItemHandler();
    }

    private void processPacket(ServerPlayer player) {
        ItemStack moduleStack = locator.getModuleStack(player);
        ItemStack filterStack = locator.getTargetItem(player);
        if (filterStack.getItem() instanceof SmartFilterItem sf) {
            GuiSyncMessage response = sf.onReceiveSettingsMessage(player, this, filterStack, moduleStack);
            if (!moduleStack.isEmpty()) {
                ModularRouterBlockEntity router = locator.getRouter(player.level()).orElse(null);
                ModuleFilterHandler filterHandler = new ModuleFilterHandler(moduleStack, router);
                filterHandler.setStackInSlot(locator.filterSlot, filterStack);
                filterHandler.save();
                if (locator.hand != null) {
                    player.setItemInHand(locator.hand, filterHandler.getHolderStack());
                } else if (router != null) {
                    router.recompileNeeded(ModularRouterBlockEntity.COMPILE_MODULES);
                }
            }
            if (response != null) {
                // send to any nearby players in case they also have the GUI open
                PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(player.getX(), player.getY(), player.getZ(),
                        8, player.getCommandSenderWorld().dimension());
                PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp), response);
            }
        }
    }

    public enum Operation {
        CLEAR_ALL, REMOVE_ITEM, MERGE, LOAD, ADD_STRING, REMOVE_AT, ANY_ALL_FLAG
    }
}
