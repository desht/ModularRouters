package me.desht.modularrouters.logic;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents the target for a given module, including the dimension, blockpos
 * and face of the block where insertion/extraction will occur.
 */
public class ModuleTarget {
    public final GlobalPos gPos;
    public final Direction face;
    public final String blockTranslationKey;

    private LazyOptional<IItemHandler> cachedItemCap = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> cachedEnergyCap = LazyOptional.empty();

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
        CompoundTag ext = new CompoundTag();
        ext.put("Pos", MiscUtil.serializeGlobalPos(gPos));
        ext.putByte("Face", (byte) face.get3DDataValue());
        ext.putString("InvName", blockTranslationKey);
        return ext;
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
        Level w = ClientUtil.theClientWorld();
        return isSameWorld(w) && getItemHandlerFor(w).isPresent();
    }

    /**
     * Get an item handler for the module target.  Only call this server-side.
     *
     * @return a (lazy optional) item handler
     */
    public LazyOptional<IItemHandler> getItemHandler() {
        return getItemHandlerFor(MiscUtil.getWorldForGlobalPos(gPos));
    }

    private LazyOptional<IItemHandler> getItemHandlerFor(Level w) {
        // called both client and server side...
        if (!cachedItemCap.isPresent()) {
            BlockPos pos = gPos.pos();
            if (w == null || !w.isLoaded(pos)) {
                cachedItemCap = LazyOptional.empty();
            } else {
                BlockEntity te = w.getBlockEntity(pos);
                cachedItemCap = te == null ? LazyOptional.empty() : te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
            }
            if (cachedItemCap.isPresent()) cachedItemCap.addListener(c -> cachedItemCap = LazyOptional.empty());
        }
        return cachedItemCap;
    }

    /**
     * Try to get an energy handler for this module target.  Only call this server-side.
     *
     * @return a (lazy optional) energy handler
     */
    public LazyOptional<IEnergyStorage> getEnergyHandler() {
        if (!cachedEnergyCap.isPresent()) {
            BlockPos pos = gPos.pos();
            Level w = MiscUtil.getWorldForGlobalPos(gPos);
            if (w == null || !w.isLoaded(pos)) {
                cachedEnergyCap = LazyOptional.empty();
            } else {
                BlockEntity te = w.getBlockEntity(pos);
                cachedEnergyCap = te == null ? LazyOptional.empty() : te.getCapability(CapabilityEnergy.ENERGY, face);
            }
            if (cachedEnergyCap.isPresent()) cachedEnergyCap.addListener(c -> cachedEnergyCap = LazyOptional.empty());
        }
        return cachedEnergyCap;
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
