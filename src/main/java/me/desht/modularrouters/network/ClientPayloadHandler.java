package me.desht.modularrouters.network;

import me.desht.modularrouters.client.gui.IResyncableGui;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.network.messages.GuiSyncMessage;
import me.desht.modularrouters.network.messages.ItemBeamMessage;
import me.desht.modularrouters.network.messages.PushEntityMessage;
import me.desht.modularrouters.network.messages.RouterUpgradesSyncMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public enum ClientPayloadHandler {
    INSTANCE;

    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleData(GuiSyncMessage message, PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            if (Minecraft.getInstance().screen instanceof IResyncableGui syncable) {
                syncable.resync(message.newStack());
            }
        });
    }

    public void handleData(ItemBeamMessage message, PlayPayloadContext context) {
        context.workHandler().submitAsync(() ->
                Minecraft.getInstance().level.getBlockEntity(message.pos(), ModBlockEntities.MODULAR_ROUTER.get())
                        .ifPresent(te -> message.beams().forEach(te::addItemBeam)));
    }

    public void handleData(PushEntityMessage message, PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(message.entityId());
            if (entity != null) {
                Vec3 vec = message.vec();
                entity.setDeltaMovement(vec.x, vec.y, vec.z);
                entity.horizontalCollision = false;
                entity.verticalCollision = false;
                if (entity instanceof LivingEntity l) l.setJumping(true);
            }
        });
    }

    public void handleData(RouterUpgradesSyncMessage message, PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null && level.isLoaded(message.pos())) {
                level.getBlockEntity(message.pos(), ModBlockEntities.MODULAR_ROUTER.get())
                        .ifPresent(router -> router.setUpgradesFrom(message.upgradesHandler()));
            }
        });
    }
}
