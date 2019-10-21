package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
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

    public FilterSettingsMessage(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        op = Operation.values()[pb.readByte()];
        locator = MFLocator.fromBuffer(pb);
        payload = pb.readCompoundTag();

    }

    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeByte(op.ordinal());
        locator.writeBuf(pb);
        pb.writeCompoundTag(payload);
    }

    public Operation getOp() {
        return op;
    }

    public CompoundNBT getPayload() {
        return payload;
    }

    public IItemHandler getTargetInventory() {
        ModuleTarget target = ModuleTarget.fromNBT(payload);
        return target.getItemHandler();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            ItemStack moduleStack = locator.getModuleStack(player);
            ItemStack filterStack = locator.getTargetItem(player);
            if (filterStack.getItem() instanceof ItemSmartFilter) {
                ItemSmartFilter sf = (ItemSmartFilter) filterStack.getItem();
                GuiSyncMessage response = sf.onReceiveSettingsMessage(player, this, filterStack, moduleStack);
                if (!moduleStack.isEmpty()) {
                    ModuleFilterHandler filterHandler = new ModuleFilterHandler(moduleStack);
                    filterHandler.setStackInSlot(locator.filterSlot, filterStack);
                    filterHandler.save();
                    if (locator.hand != null) {
                        player.setHeldItem(locator.hand, filterHandler.getHolderStack());
                    } else if (locator.routerPos != null) {
                        TileEntityItemRouter.getRouterAt(player.world, locator.routerPos)
                                .ifPresent(router -> router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES));
                    }
                }
                if (response != null) {
                    // send to any nearby players in case they also have the GUI open
                    PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(player.posX, player.posY, player.posZ,
                            8, player.getEntityWorld().getDimension().getType());
                    PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp), response);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
