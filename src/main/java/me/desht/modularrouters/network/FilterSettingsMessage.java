package me.desht.modularrouters.network;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 *
 * Sent when a filter's settings have been changed in any way via its GUI.
 * The filter could be in a player's hand, or in a module (which may or may not be in a router...)
 */
public class FilterSettingsMessage {
    private Operation op;
    private CompoundNBT payload;
    private MFLocator locator;

    public enum Operation {
        CLEAR_ALL, REMOVE_ITEM, MERGE, LOAD, ADD_STRING, REMOVE_AT, ANY_ALL_FLAG
    }

    public FilterSettingsMessage() {
    }

    public FilterSettingsMessage(Operation op, MFLocator locator, CompoundNBT payload) {
        this.op = op;
        this.locator = locator;
        this.payload = payload;
    }

    public FilterSettingsMessage(PacketBuffer buf) {
        op = Operation.values()[buf.readByte()];
        locator = MFLocator.fromBuffer(buf);
        payload = buf.readCompoundTag();

    }

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(op.ordinal());
        locator.writeBuf(buf);
        buf.writeCompoundTag(payload);
    }

    public Operation getOp() {
        return op;
    }

    public CompoundNBT getPayload() {
        return payload;
    }

    public LazyOptional<IItemHandler> getTargetInventory() {
        ModuleTarget target = ModuleTarget.fromNBT(payload);
        return target.getItemHandler();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                processPacket(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void processPacket(ServerPlayerEntity player) {
        ItemStack moduleStack = locator.getModuleStack(player);
        ItemStack filterStack = locator.getTargetItem(player);
        if (filterStack.getItem() instanceof ItemSmartFilter) {
            ItemSmartFilter sf = (ItemSmartFilter) filterStack.getItem();
            GuiSyncMessage response = sf.onReceiveSettingsMessage(player, this, filterStack, moduleStack);
            if (!moduleStack.isEmpty()) {
                TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.world, locator.routerPos).orElse(null);
                ModuleFilterHandler filterHandler = new ModuleFilterHandler(moduleStack, router);
                filterHandler.setStackInSlot(locator.filterSlot, filterStack);
                filterHandler.save();
                if (locator.hand != null) {
                    player.setHeldItem(locator.hand, filterHandler.getHolderStack());
                } else if (router != null) {
                    router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
                }
            }
            if (response != null) {
                // send to any nearby players in case they also have the GUI open
                PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(player.getPosX(), player.getPosY(), player.getPosZ(),
                        8, player.getEntityWorld().getDimensionKey());
                PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp), response);
            }
        }
    }
}
