package me.desht.modularrouters.network;

import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.module.TargetedModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when a module is left-clicked; ask the server to validate the module and
 * send the player a message.
 */
public class ValidateModuleMessage {
    private final InteractionHand hand;

    public ValidateModuleMessage(InteractionHand hand) {
        this.hand = hand;
    }

    public ValidateModuleMessage(FriendlyByteBuf buf) {
        hand = buf.readEnum(InteractionHand.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(hand);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof ModuleItem moduleItem) {
                    moduleItem.doModuleValidation(stack, player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
