package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.container.BulkItemFilterMenu;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.Optional;

/**
 * Received on: SERVER
 * <p>
 * Sent when a bulk filter clear/load/merge button is pressed to modify its contents server-side
 * The filter could be in a player's hand, or in a module (which may or may not be in a router...)
 */
public record BulkFilterUpdateMessage(FilterOp op, MFLocator locator, Optional<ModuleTarget> targetInv) implements CustomPacketPayload {
    public static final Type<BulkFilterUpdateMessage> TYPE = new Type<>(MiscUtil.RL("filter_settings"));

    public static final StreamCodec<FriendlyByteBuf, BulkFilterUpdateMessage> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(FilterOp.class), BulkFilterUpdateMessage::op,
            MFLocator.STREAM_CODEC, BulkFilterUpdateMessage::locator,
            ModuleTarget.STREAM_CODEC.apply(ByteBufCodecs::optional), BulkFilterUpdateMessage::targetInv,
            BulkFilterUpdateMessage::new
    );

    public static BulkFilterUpdateMessage targeted(FilterOp op, MFLocator locator, ModuleTarget target) {
        return new BulkFilterUpdateMessage(op, locator, Optional.of(target));
    }

    public static BulkFilterUpdateMessage untargeted(FilterOp op, MFLocator locator) {
        return new BulkFilterUpdateMessage(op, locator, Optional.empty());
    }

    @Override
    public Type<BulkFilterUpdateMessage> type() {
        return TYPE;
    }

    public Optional<ResourceHandler<ItemResource>> getTargetInventory() {
        return targetInv.flatMap(ModuleTarget::getItemHandler);
    }

    public static void handleData(BulkFilterUpdateMessage message, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        MFLocator locator = message.locator();

        if (locator.getTargetItem(player).getItem() instanceof BulkItemFilter && player.containerMenu instanceof BulkItemFilterMenu filterMenu) {
            ModuleFlags flags = ModuleFlags.forItem(locator.getModuleStack(player));
            switch (message.op()) {
                case CLEAR_ALL -> filterMenu.clearSlots();
                case MERGE -> message.getTargetInventory()
                        .ifPresent(h -> filterMenu.mergeInventory(h, flags, false));
                case LOAD -> message.getTargetInventory()
                        .ifPresent(h -> filterMenu.mergeInventory(h, flags, true));
            }
        }
    }

    public enum FilterOp {
        CLEAR_ALL, MERGE, LOAD
    }
}
