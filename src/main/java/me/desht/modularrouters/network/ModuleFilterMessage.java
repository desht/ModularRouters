package me.desht.modularrouters.network;

import me.desht.modularrouters.container.ContainerBulkItemFilter;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.container.FilterSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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

    public ModuleFilterMessage(FriendlyByteBuf buffer) {
        slot = buffer.readByte();
        stack = buffer.readItem();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeByte(slot);
        buffer.writeItem(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player != null) {
                AbstractContainerMenu c = player.containerMenu;
                if (isValidContainer(c) && slot >= 0 && slot < c.slots.size() && c.getSlot(slot) instanceof FilterSlot) {
                    c.getSlot(slot).set(stack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private boolean isValidContainer(AbstractContainerMenu c) {
        return c instanceof ContainerModule || c instanceof ContainerBulkItemFilter;
    }
}
