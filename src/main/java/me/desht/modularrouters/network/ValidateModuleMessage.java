package me.desht.modularrouters.network;

import me.desht.modularrouters.item.module.TargetedModule;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when a targeted module is left-clicked; ask the server to validate the module and
 * send the player a message.
 */
public class ValidateModuleMessage {
    private Hand hand;

    public ValidateModuleMessage() {
    }

    public ValidateModuleMessage(Hand hand) {
        this.hand = hand;
    }

    public ValidateModuleMessage(PacketBuffer buf) {
        hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() instanceof TargetedModule) {
                    ((TargetedModule) stack.getItem()).doModuleValidation(stack, player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
