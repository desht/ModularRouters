package me.desht.modularrouters.network;

import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.container.FilterSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when a module filter slot is updated via JEI
 */
public class ModuleFilterMessage {
    private final int slot;
    private final ItemStack stack;

    public ModuleFilterMessage(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    public ModuleFilterMessage(PacketBuffer buffer) {
        slot = buffer.readByte();
        stack = buffer.readItem();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(slot);
        buffer.writeItem(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            Container c = player.containerMenu;
            if (c instanceof ContainerModule && slot >= 0 && slot < c.slots.size() && c.getSlot(slot) instanceof FilterSlot) {
                c.getSlot(slot).set(stack);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
