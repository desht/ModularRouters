package me.desht.modularrouters.network.messages;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Received on: SERVER
 * <p>
 * Sent by client when a player updates a module's settings via its GUI.
 */
public record ModuleSettingsMessage(MFLocator locator, DataComponentPatch patch) implements CustomPacketPayload {
    public static final Type<ModuleSettingsMessage> TYPE = new Type<>(MiscUtil.RL("module_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ModuleSettingsMessage> STREAM_CODEC = StreamCodec.composite(
            MFLocator.STREAM_CODEC, ModuleSettingsMessage::locator,
            DataComponentPatch.STREAM_CODEC, ModuleSettingsMessage::patch,
            ModuleSettingsMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(ModuleSettingsMessage message, IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof ModuleMenu menu) || !menu.getLocator().equals(message.locator) || !validateComponents(message.patch)) {
            return;
        }

        MFLocator locator = message.locator();
        ItemStack moduleStack = locator.getModuleStack(player);

        if (moduleStack.getItem() instanceof ModuleItem && moduleStack.getComponents() instanceof PatchedDataComponentMap pdcm) {
            pdcm.applyPatch(message.patch);
            locator.setModuleStack(player, moduleStack);
            locator.getRouter(player).ifPresent(router -> router.recompileNeeded(ModularRouterBlockEntity.RecompileFlag.MODULES));
        }
    }

    private static boolean validateComponents(DataComponentPatch patch) {
        return patch.entrySet().stream()
                .allMatch(c -> ModDataComponents.SETTINGS_COMPONENTS.get().contains(c.getKey()));
    }
}
