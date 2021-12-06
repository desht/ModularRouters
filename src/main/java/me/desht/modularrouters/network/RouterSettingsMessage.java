package me.desht.modularrouters.network;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 *
 * Sent by client to update router settings from GUI
 * Sent by server to sync router settings when GUI is opened
 */
public class RouterSettingsMessage {
    private final boolean ecoMode;
    private final RouterRedstoneBehaviour redstoneBehaviour;
    private final ModularRouterBlockEntity.EnergyDirection energyDirection;
    private final BlockPos pos;

    public RouterSettingsMessage(ModularRouterBlockEntity router) {
        this.pos = router.getBlockPos();
        this.redstoneBehaviour = router.getRedstoneBehaviour();
        this.ecoMode = router.getEcoMode();
        this.energyDirection = router.getEnergyDirection();
    }

    RouterSettingsMessage(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        redstoneBehaviour = RouterRedstoneBehaviour.values()[buffer.readByte()];
        ecoMode = buffer.readBoolean();
        energyDirection = buffer.readEnum(ModularRouterBlockEntity.EnergyDirection.class);
    }

    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeBlockPos(pos);
        byteBuf.writeByte(redstoneBehaviour.ordinal());
        byteBuf.writeBoolean(ecoMode);
        byteBuf.writeEnum(energyDirection);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level w = ctx.get().getSender() == null ? ClientUtil.theClientWorld() : ctx.get().getSender().getLevel();
            w.getBlockEntity(pos, ModBlockEntities.MODULAR_ROUTER.get()).ifPresent(router -> {
                router.setRedstoneBehaviour(redstoneBehaviour);
                router.setEcoMode(ecoMode);
                router.setEnergyDirection(energyDirection);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
