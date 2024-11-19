package me.desht.modularrouters.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a target for a given targeted module, including the dimension, blockpos
 *  and face of the block where insertion/extraction will occur.
 * Targeted modules store one or more of these objects; see also {@link ModuleTargetList}
 */
public class ModuleTarget {
    public final GlobalPos gPos;
    public final Direction face;
    public final String blockTranslationKey;

    // Expected max size of 2 given a 2/3 load factor, this is reasonable enough for most routers
    private final IdentityHashMap<BlockCapability<?, ?>, BlockCapabilityCache<?, ?>> capCache = new IdentityHashMap<>(4);

    public static final Codec<ModuleTarget> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(t -> t.gPos),
            Direction.CODEC.fieldOf("face").forGetter(t -> t.face),
            Codec.STRING.optionalFieldOf("key", "").forGetter(t -> t.blockTranslationKey)
    ).apply(builder, ModuleTarget::new));

    public static final StreamCodec<FriendlyByteBuf,ModuleTarget> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, t -> t.gPos,
            Direction.STREAM_CODEC, t -> t.face,
            ByteBufCodecs.STRING_UTF8, t -> t.blockTranslationKey,
            ModuleTarget::new
    );

    public ModuleTarget(GlobalPos gPos, Direction face, String blockTranslationKey) {
        this.gPos = gPos;
        this.face = face;
        this.blockTranslationKey = blockTranslationKey;
    }

    public ModuleTarget(GlobalPos gPos, Direction face) {
        this(gPos, face, "");
    }

    public ModuleTarget(GlobalPos gPos) {
        this(gPos, null);
    }

    public boolean isSameWorld(@Nullable Level world) {
        return world != null && gPos.dimension() == world.dimension();
    }

    public boolean isSameWorld(ModuleTarget dst) {
        return gPos.dimension() == dst.gPos.dimension();
    }

    /**
     * Check for existence of an item handler.  The target dimension must be the same as the level's current dimension.
     *
     * @return a (lazy optional) item handler
     */
    public boolean hasItemHandler(Level level) {
        return level.dimension().location().equals(gPos.dimension().location())
                && level.getCapability(Capabilities.ItemHandler.BLOCK, gPos.pos(), face) != null;
    }

    /**
     * Get an item handler for the module target.  Only call this server-side.
     *
     * @return an optional item handler
     */
    public Optional<IItemHandler> getItemHandler() {
        return getCapability(Capabilities.ItemHandler.BLOCK);
    }

    /**
     * Try to get a fluid handler for this module target.  Only call this server-side.
     *
     * @return an optional fluid handler
     */
    public Optional<IFluidHandler> getFluidHandler() {
        return getCapability(Capabilities.FluidHandler.BLOCK);
    }

    /**
     * Try to get an energy handler for this module target.  Only call this server-side.
     *
     * @return an optional energy handler
     */
    public Optional<IEnergyStorage> getEnergyHandler() {
        return getCapability(Capabilities.EnergyStorage.BLOCK);
    }

    /**
     * Try to get a capability from the target. Only call this server-side.
     *
     * @param cap the capability
     * @param <T> the capability type
     * @return an optional capability
     * @implNote If the capability has the context of {@linkplain Direction}, the context will be the face of this target, otherwise it will be null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Optional<T> getCapability(BlockCapability<T, ?> cap) {
        ServerLevel level = MiscUtil.getWorldForGlobalPos(gPos);
        if (level == null) {
            return Optional.empty();
        }

        var cache = capCache.get(cap);
        if (cache == null) {
            if (cap.contextClass() == Direction.class) {
                cache = BlockCapabilityCache.create((BlockCapability) cap, level, gPos.pos(), face);
            } else {
                cache = BlockCapabilityCache.create((BlockCapability) cap, level, gPos.pos(), null);
            }

            capCache.put(cap, cache);
        }

        return Optional.ofNullable((T) cache.getCapability());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleTarget that)) return false;
        return gPos.equals(that.gPos) && face == that.face;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gPos, face);
    }

    @Override
    public String toString() {
        return MiscUtil.locToString(gPos) + " " + face;
    }

    public Component getTextComponent() {
        return Component.translatable(blockTranslationKey).withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" @ " + this).withStyle(ChatFormatting.AQUA));
    }
}
