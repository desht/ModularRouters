package me.desht.modularrouters.network;

import me.desht.modularrouters.container.BulkItemFilterMenu;
import me.desht.modularrouters.container.FilterSlot;
import me.desht.modularrouters.container.ModuleMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

/**
 * Received on: SERVER
 * Sent by client when a filter slot is updated via the JEI ghost handler
 */
public class ModuleFilterMessage implements SimpleMessage {
    private final int slot;
    private final ItemStack stack;

    public ModuleFilterMessage(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    public ModuleFilterMessage(FriendlyByteBuf buffer) {
        slot = buffer.readVarInt();
        stack = buffer.readItem();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slot);
        buffer.writeItem(stack);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            AbstractContainerMenu c = player.containerMenu;
            if (isValidContainer(c) && slot >= 0 && slot < c.slots.size() && c.getSlot(slot) instanceof FilterSlot) {
                c.getSlot(slot).set(stack);
            }
        }
    }

    private boolean isValidContainer(AbstractContainerMenu c) {
        return c instanceof ModuleMenu || c instanceof BulkItemFilterMenu;
    }
}
