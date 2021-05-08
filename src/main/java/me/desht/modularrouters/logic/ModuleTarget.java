package me.desht.modularrouters.logic;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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

    public CompoundNBT toNBT() {
        CompoundNBT ext = new CompoundNBT();
        ext.put("Pos", MiscUtil.serializeGlobalPos(gPos));
        ext.putByte("Face", (byte) face.ordinal());
        ext.putString("InvName", blockTranslationKey);
        return ext;
    }

    public static ModuleTarget fromNBT(CompoundNBT nbt) {
        GlobalPos gPos = MiscUtil.deserializeGlobalPos(nbt.getCompound("Pos"));
        Direction face = Direction.values()[nbt.getByte("Face")];
        return new ModuleTarget(gPos, face, nbt.getString("InvName"));
    }

    public boolean isSameWorld(World world) {
        return gPos.dimension() == world.dimension();
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
        World w = ClientUtil.theClientWorld();
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

    private LazyOptional<IItemHandler> getItemHandlerFor(World w) {
        // called both client and server side...
        if (!cachedItemCap.isPresent()) {
            BlockPos pos = gPos.pos();
            if (w == null || !w.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                cachedItemCap = LazyOptional.empty();
            } else {
                TileEntity te = w.getBlockEntity(pos);
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
            World w = MiscUtil.getWorldForGlobalPos(gPos);
            if (w == null || !w.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                cachedEnergyCap = LazyOptional.empty();
            } else {
                TileEntity te = w.getBlockEntity(pos);
                cachedEnergyCap = te == null ? LazyOptional.empty() : te.getCapability(CapabilityEnergy.ENERGY, face);
            }
            if (cachedEnergyCap.isPresent()) cachedEnergyCap.addListener(c -> cachedEnergyCap = LazyOptional.empty());
        }
        return cachedEnergyCap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleTarget)) return false;
        ModuleTarget that = (ModuleTarget) o;
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

    public ITextComponent getTextComponent() {
        return new TranslationTextComponent(blockTranslationKey).withStyle(TextFormatting.WHITE)
                .append(new StringTextComponent(" @ " + toString()).withStyle(TextFormatting.AQUA));
    }
}
