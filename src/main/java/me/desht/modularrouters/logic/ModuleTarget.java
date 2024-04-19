package me.desht.modularrouters.logic;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the target for a given module, including the dimension, blockpos
 * and face of the block where insertion/extraction will occur.
 */
public class ModuleTarget {
    public final GlobalPos gPos;
    public final Direction face;
    public final String blockTranslationKey;

    private BlockCapabilityCache<IItemHandler,Direction> itemCapCache;
    private BlockCapabilityCache<IFluidHandler,Direction> fluidCapCache;
    private BlockCapabilityCache<IEnergyStorage,Direction> energyCapCache;
    private final Map<BlockCapability<?, ?>, BlockCapabilityCache<?, ?>> capabilityCache = new IdentityHashMap<>();

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

    public CompoundTag toNBT() {
        return Util.make(new CompoundTag(), ext -> {
            ext.put("Pos", MiscUtil.serializeGlobalPos(gPos));
            ext.putByte("Face", (byte) face.get3DDataValue());
            ext.putString("InvName", blockTranslationKey);
        });
    }

    public static ModuleTarget fromNBT(CompoundTag nbt) {
        GlobalPos gPos = MiscUtil.deserializeGlobalPos(nbt.getCompound("Pos"));
        Direction face = Direction.from3DDataValue(nbt.getByte("Face"));
        return new ModuleTarget(gPos, face, nbt.getString("InvName"));
    }

    public boolean isSameWorld(@Nullable Level world) {
        return world != null && gPos.dimension() == world.dimension();
    }

    public boolean isSameWorld(ModuleTarget dst) {
        return gPos.dimension() == dst.gPos.dimension();
    }

    /**
     * Check for existence of an item handler client-side.  The target dimension must be the same as the client's
     * current dimension.
     *
     * @return a (lazy optional) item handler
     */
    public boolean hasItemHandlerClientSide() {
        return ClientUtil.theClientLevel().getCapability(Capabilities.ItemHandler.BLOCK, gPos.pos(), face) != null;
    }

    /**
     * Get an item handler for the module target.  Only call this server-side.
     *
     * @return an optional item handler
     */
    public Optional<IItemHandler> getItemHandler() {
        if (itemCapCache == null) {
            ServerLevel level = MiscUtil.getWorldForGlobalPos(gPos);
            if (level == null) {
                return Optional.empty();
            }
            itemCapCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, level, gPos.pos(), face);
        }

        return Optional.ofNullable(itemCapCache.getCapability());
    }

    /**
     * Try to get a fluid handler for this module target.  Only call this server-side.
     *
     * @return an optional fluid handler
     */
    public Optional<IFluidHandler> getFluidHandler() {
        if (fluidCapCache == null) {
            ServerLevel level = MiscUtil.getWorldForGlobalPos(gPos);
            if (level == null) {
                return Optional.empty();
            }
            fluidCapCache = BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, level, gPos.pos(), face);
        }
        return Optional.ofNullable(fluidCapCache.getCapability());
    }

    /**
     * Try to get an energy handler for this module target.  Only call this server-side.
     *
     * @return an optional energy handler
     */
    public Optional<IEnergyStorage> getEnergyHandler() {
        if (energyCapCache == null) {
            ServerLevel level = MiscUtil.getWorldForGlobalPos(gPos);
            if (level == null) {
                return Optional.empty();
            }
            energyCapCache = BlockCapabilityCache.create(Capabilities.EnergyStorage.BLOCK, level, gPos.pos(), face);
        }
        return Optional.ofNullable(energyCapCache.getCapability());
    }

    /**
     * Get a cached capability of the module target.
     *
     * @param capability the capability
     * @param context    the capability context
     * @return the capability
     */
    @Nullable
    public <T, C> T getCapability(BlockCapability<T, C> capability, @Nullable C context) {
        var cached = (BlockCapabilityCache<T, C>)capabilityCache.get(capability);
        if (cached != null && Objects.equals(cached.context(), context)) {
            return cached.getCapability();
        }
        ServerLevel level = MiscUtil.getWorldForGlobalPos(gPos);
        if (level == null) {
            return null;
        }
        cached = BlockCapabilityCache.create(capability, level, gPos.pos(), context);
        capabilityCache.put(capability, cached);
        return cached.getCapability();
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
