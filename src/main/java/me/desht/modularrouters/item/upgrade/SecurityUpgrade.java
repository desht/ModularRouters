package me.desht.modularrouters.item.upgrade;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.util.TranslatableEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SecurityUpgrade extends UpgradeItem implements IPlayerOwned {
    private static final int MAX_PLAYERS = 6;

    public SecurityUpgrade(Properties properties) {
        super(properties.component(ModDataComponents.SECURITY_LIST, SecurityList.DEFAULT));
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, Consumer<Component> consumer) {
        String owner = getOwnerProfile(itemstack).map(GameProfile::name).orElse("-");

        consumer.accept(ClientUtil.xlate("modularrouters.itemText.security.owner", ChatFormatting.AQUA + owner));
        Set<String> names = getPlayerNames(itemstack);
        if (!names.isEmpty()) {
            consumer.accept(ClientUtil.xlate("modularrouters.itemText.security.count", names.size(), MAX_PLAYERS));

            names.stream()
                    .map(name -> " • " + ChatFormatting.YELLOW + name)
                    .sorted()
                    .map(Component::literal)
                    .forEach(consumer);
        }
    }

    @Override
    public void onCompiled(ItemStack stack, ModularRouterBlockEntity router) {
        super.onCompiled(stack, router);

        router.addPermittedIds(getPlayerIDs(stack));
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(64, 64, 255);
    }

    private Set<UUID> getPlayerIDs(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SECURITY_LIST, SecurityList.DEFAULT).trusted().stream()
                .map(tp -> tp.partialProfile().id())
                .collect(Collectors.toSet());
    }

    /**
     * Get a set of player names added to this security upgrade, not including the owner.
     *
     * @param stack the upgrade itemstack
     * @return set of (displayable) player names
     */
    private static Set<String> getPlayerNames(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SECURITY_LIST, SecurityList.DEFAULT).trusted().stream()
                .map(tp -> tp.partialProfile().name())
                .collect(Collectors.toSet());
    }

    private static Result addPlayer(ItemStack stack, GameProfile profile) {
        SecurityList securityList = stack.getOrDefault(ModDataComponents.SECURITY_LIST, SecurityList.DEFAULT);
        if (securityList.trusted.size() >= MAX_PLAYERS) {
            return Result.FULL;
        }

        SecurityList newList = securityList.add(profile);
        if (newList.trusted.size() == securityList.trusted.size()) {
            return Result.ALREADY_ADDED;
        }

        stack.set(ModDataComponents.SECURITY_LIST, newList);

        return Result.ADDED;
    }

    private static Result removePlayer(ItemStack stack, GameProfile profile) {
        SecurityList securityList = stack.getOrDefault(ModDataComponents.SECURITY_LIST, SecurityList.DEFAULT);

        SecurityList newList = securityList.remove(profile);
        if (newList.trusted.size() == securityList.trusted.size()) {
            return Result.NOT_PRESENT;
        }

        stack.set(ModDataComponents.SECURITY_LIST, newList);

        return Result.REMOVED;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.level().isClientSide() && player.isSteppingCarefully()) {
            setOwner(stack, player);
            Component displayName = Objects.requireNonNullElse(player.getDisplayName(), Component.literal("?"));
            player.displayClientMessage(Component.translatable("modularrouters.itemText.security.owner", displayName.getString()), false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof Player targetPlayer) {
            GameProfile profile = targetPlayer.getGameProfile();
            Result res = player.isSteppingCarefully() ? removePlayer(stack, profile) : addPlayer(stack, profile);
            if (player.level().isClientSide()) {
                player.playSound(res.isError() ? ModSounds.ERROR.get() : ModSounds.SUCCESS.get(),
                        ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.translatable(res.getTranslationKey(), profile.name()), false);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    enum Result implements TranslatableEnum, StringRepresentable {
        ADDED("added"),
        REMOVED("removed"),
        FULL("full"),
        ALREADY_ADDED("already_added"),
        ERROR("error"),
        NOT_PRESENT("not_present");

        private final String name;

        Result(String name) {
            this.name = name;
        }

        boolean isError() {
            return this != ADDED && this != REMOVED;
        }

        @Override
        public String getTranslationKey() {
            return "modularrouters.chatText.security." + getSerializedName();
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record SecurityList(List<ResolvableProfile> trusted) {
        public static final SecurityList DEFAULT = new SecurityList(List.of());

        public static final Codec<SecurityList> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ResolvableProfile.CODEC.listOf(0, MAX_PLAYERS).fieldOf("trusted").forGetter(SecurityList::trusted)
        ).apply(builder, SecurityList::new));

        public static StreamCodec<FriendlyByteBuf, SecurityList> STREAM_CODEC = StreamCodec.composite(
                ResolvableProfile.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_PLAYERS)), SecurityList::trusted,
                SecurityList::new
        );

        public SecurityList add(GameProfile profile) {
            Set<ResolvableProfile> l = new HashSet<>(trusted);
            l.add(ResolvableProfile.createResolved(profile));
            return new SecurityList(List.copyOf(l));
        }

        public SecurityList remove(GameProfile profile) {
            // TODO: Partial profile might not equal profile.
            List<ResolvableProfile> l = trusted.stream()
                    .filter(rp -> !rp.partialProfile().equals(profile))
                    .toList();
            return new SecurityList(l);
        }
    }
}
