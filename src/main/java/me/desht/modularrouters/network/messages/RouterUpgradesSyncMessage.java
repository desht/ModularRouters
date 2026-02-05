package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.util.CodecUtil;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;

/**
 * Received on: CLIENT
 *  <p>
 * Sent when a router GUI is opened to sync all the upgrades to the clientside block entity.
 * Various GUI messages/tooltips/etc. depend on knowing what upgrades the router has.
 */
public record RouterUpgradesSyncMessage(BlockPos pos, ItemStacksResourceHandler upgradesHandler) implements CustomPacketPayload {
    public static final Type<RouterUpgradesSyncMessage> TYPE = new Type<>(MiscUtil.RL("router_upgrades_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf,RouterUpgradesSyncMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RouterUpgradesSyncMessage::pos,
            CodecUtil.ITEM_HANDLER_STREAM_CODEC, RouterUpgradesSyncMessage::upgradesHandler,
            RouterUpgradesSyncMessage::new
    );

    public static RouterUpgradesSyncMessage forRouter(ModularRouterBlockEntity router) {
        BlockPos pos = router.getBlockPos();
        ResourceHandler<ItemResource> h = router.getUpgrades();
        ItemStacksResourceHandler handler = new ItemStacksResourceHandler(h.size());
        for (int i = 0; i < h.size(); i++) {
            handler.set(i, ItemResource.of(ItemUtil.getStack(h, i)), ItemUtil.getStack(h, i).getCount());
        }
        return new RouterUpgradesSyncMessage(pos, handler);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(RouterUpgradesSyncMessage message, IPayloadContext context) {
        Level level = context.player().level();
        if (level.isLoaded(message.pos())) {
            level.getBlockEntity(message.pos(), ModBlockEntities.MODULAR_ROUTER.get())
                    .ifPresent(router -> router.setUpgradesFrom(message.upgradesHandler()));
        }
    }
}
