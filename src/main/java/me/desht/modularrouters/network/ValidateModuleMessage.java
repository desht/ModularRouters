package me.desht.modularrouters.network;

import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.simple.SimpleMessage;

/**
 * Received on: SERVER
 * Sent by client when a module is left-clicked; ask the server to validate the module and
 * send the player a message.
 */
public class ValidateModuleMessage implements SimpleMessage {
    private final InteractionHand hand;

    public ValidateModuleMessage(InteractionHand hand) {
        this.hand = hand;
    }

    public ValidateModuleMessage(FriendlyByteBuf buf) {
        hand = buf.readEnum(InteractionHand.class);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
    }

    @Override
    public void handleMainThread(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ModuleItem moduleItem) {
                moduleItem.doModuleValidation(stack, player);
            }
        }
    }
}
