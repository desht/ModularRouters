package me.desht.modularrouters.logic;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
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
        return gPos.func_239646_a_() == world.func_234923_W_();
    }

    public boolean isSameWorld(ModuleTarget dst) {
        return gPos.func_239646_a_() == dst.gPos.func_239646_a_();
    }

    /**
     * Client-side version.  The target dimension must be the same as the client's current dimension, and the
     * resulting item handler should ONLY be used to test presence with {@link LazyOptional#isPresent()}.
     * @param w the client world
     * @return a (lazy optional) item handler
     */
    public LazyOptional<IItemHandler> getItemHandler(World w) {
        return isSameWorld(w) ? getItemHandlerFor(w) : LazyOptional.empty();
    }

    /**
     * Server-side version.  Get an item handler for the module target.
     *
     * @return a (lazy optional) item handler
     */
    public LazyOptional<IItemHandler> getItemHandler() {
        return getItemHandlerFor(MiscUtil.getWorldForGlobalPos(gPos));
    }

    /**
     * Try to get an item handler object for this module target.  Can be run server- or client-side, but if run
     * client-side will only return an item handler IF the target dimension is the same as the current client
     * dimension. In addition, the client-side item handler should <strong>only</strong> be used for testing the
     * presence of an inventory at the target with {@link LazyOptional#isPresent()}.
     *
     * @return a (lazy optional) item handler
     */
    private LazyOptional<IItemHandler> getItemHandlerFor(World w) {
        BlockPos pos = gPos.getPos();
        if (w == null || !w.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4))
            return LazyOptional.empty();
        TileEntity te = w.getTileEntity(pos);
        return te == null ? LazyOptional.empty() : te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
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
        return MiscUtil.xlate(blockTranslationKey).func_240699_a_(TextFormatting.WHITE)
                .func_230529_a_(new StringTextComponent(" @ " + toString()).func_240699_a_(TextFormatting.AQUA));
    }
}
