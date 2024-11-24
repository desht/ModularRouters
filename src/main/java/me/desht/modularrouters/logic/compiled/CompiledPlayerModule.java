package me.desht.modularrouters.logic.compiled;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.logic.settings.TransferDirection;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.TranslatableEnum;
import me.desht.modularrouters.util.WildcardedRLMatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.*;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class CompiledPlayerModule extends CompiledModule {
    private final PlayerSettings settings;
    private final GameProfile playerProfile;

    private WeakReference<Player> playerRef;

    public CompiledPlayerModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.PLAYER_SETTINGS, PlayerSettings.DEFAULT);
        playerProfile = ((IPlayerOwned) stack.getItem()).getOwnerProfile(stack).orElse(null);

        if (router != null && !router.nonNullLevel().isClientSide) {
            Player player = playerProfile == null ? null : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerProfile.getId());
            playerRef = new WeakReference<>(player);
        } else {
            playerRef = new WeakReference<>(null);
        }
    }

    @Override
    public boolean shouldExecute() {
        return getPlayer() != null;
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        Player player = getPlayer();  // will be non-null if we get here

        if (isDimensionBlacklisted(router, player)) {
            return false;
        }

        IItemHandler itemHandler = getHandler(player);

        ItemStack bufferStack = router.getBufferItemStack();
        switch (getTransferDirection()) {
            case TO_ROUTER -> {
                if (bufferStack.getCount() < bufferStack.getMaxStackSize()) {
                    ItemStack taken = transferToRouter(itemHandler, null, router);
                    return !taken.isEmpty();
                }
            }
            case FROM_ROUTER -> {
                if (getFilter().test(bufferStack)) {
                    if (getSection() == Section.ARMOR) {
                        return insertArmor(router, player, itemHandler, bufferStack);
                    } else {
                        int nToSend = getItemsPerTick(router);
                        if (getRegulationAmount() > 0) {
                            int existing = InventoryUtils.countItems(bufferStack, itemHandler, getRegulationAmount(), !getFilter().getFlags().matchDamage());
                            nToSend = Math.min(nToSend, getRegulationAmount() - existing);
                            if (nToSend <= 0) {
                                return false;
                            }
                        }
                        int sent = InventoryUtils.transferItems(router.getBuffer(), itemHandler, 0, nToSend);
                        return sent > 0;
                    }
                }
            }
            default -> {
                return false;
            }
        }
        return false;
    }

    private boolean isDimensionBlacklisted(ModularRouterBlockEntity router, Player player) {
        WildcardedRLMatcher matcher = ModularRouters.getDimensionBlacklist();
        return matcher.test(router.nonNullLevel().dimension().location()) || matcher.test(player.level().dimension().location());
    }

    private Player getPlayer() {
        return playerRef == null ? null : playerRef.get();
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().getUUID().equals(getPlayerId())) {
            playerRef = new WeakReference<>(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getUUID().equals(getPlayerId())) {
            playerRef = new WeakReference<>(null);
        }
    }

    @Override
    public void onCompiled(ModularRouterBlockEntity router) {
        super.onCompiled(router);
        if (!router.nonNullLevel().isClientSide) {
            NeoForge.EVENT_BUS.register(this);
        }
    }

    @Override
    public void cleanup(ModularRouterBlockEntity router) {
        super.cleanup(router);
        if (!router.nonNullLevel().isClientSide) {
            NeoForge.EVENT_BUS.unregister(this);
        }
    }

    public UUID getPlayerId() {
        return playerProfile == null ? null : playerProfile.getId();
    }

    public String getPlayerName() {
        return playerProfile == null ? null : playerProfile.getName();
    }

    public TransferDirection getTransferDirection() {
        return settings.direction;
    }

    public Section getSection() {
        return settings.section;
    }

    private boolean insertArmor(ModularRouterBlockEntity router, Player player, IItemHandler itemHandler, ItemStack armorStack) {
        int slot = getSlotForArmorItem(player, armorStack);
        if (slot >= 0 && itemHandler.getStackInSlot(slot).isEmpty()) {
            ItemStack extracted = router.getBuffer().extractItem(0, 1, false);
            if (extracted.isEmpty()) {
                return false;
            }
            ItemStack res = itemHandler.insertItem(slot, extracted, false);
            return res.isEmpty();
        } else {
            return false;
        }
    }

    private int getSlotForArmorItem(LivingEntity entity, ItemStack stack) {
        return switch (entity.getEquipmentSlotForItem(stack)) {
            case HEAD -> 3;
            case CHEST -> 2;
            case LEGS -> 1;
            case FEET -> 0;
            default -> -1;
        };
    }

    private IItemHandler getHandler(Player player) {
        return switch (getSection()) {
            case MAIN -> new PlayerMainInvWrapper(player.getInventory());
            case MAIN_NO_HOTBAR -> new PlayerMainInvNoHotbarWrapper(player.getInventory());
            case ARMOR -> new PlayerArmorInvWrapper(player.getInventory());
            case OFFHAND -> new PlayerOffhandInvWrapper(player.getInventory());
            case ENDER -> new InvWrapper(player.getEnderChestInventory());
        };
    }

    public static class PlayerMainInvNoHotbarWrapper extends RangedWrapper {
        PlayerMainInvNoHotbarWrapper(Inventory inv) {
            super(new InvWrapper(inv), Inventory.getSelectionSize(), inv.items.size());
        }
    }

    public enum Section implements TranslatableEnum, StringRepresentable {
        MAIN("main"),
        MAIN_NO_HOTBAR("main_no_hotbar"),
        ARMOR("armor"),
        OFFHAND("offhand"),
        ENDER("ender");

        private final String name;

        Section(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.label.playerSect." + name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record PlayerSettings(TransferDirection direction, Section section) {
        public static final PlayerSettings DEFAULT = new PlayerSettings(TransferDirection.TO_ROUTER, Section.MAIN);

        public static final Codec<PlayerSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                StringRepresentable.fromEnum(TransferDirection::values)
                        .optionalFieldOf("direction", TransferDirection.TO_ROUTER)
                        .forGetter(PlayerSettings::direction),
                StringRepresentable.fromEnum(Section::values)
                        .optionalFieldOf("section", Section.MAIN)
                        .forGetter(PlayerSettings::section)
        ).apply(builder, PlayerSettings::new));

        public static StreamCodec<FriendlyByteBuf, PlayerSettings> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(TransferDirection.class), PlayerSettings::direction,
                NeoForgeStreamCodecs.enumCodec(Section.class), PlayerSettings::section,
                PlayerSettings::new
        );
    }
}
