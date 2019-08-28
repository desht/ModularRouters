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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.system.CallbackI;

import java.util.Objects;

/**
 * Represents the blockpos that a module in a particular router has targeted, including the dimension
 * and face of the block where insertion/extraction will occur.
 */
public class ModuleTarget {
//    public final int dimId;
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
        return gPos.getDimension() == world.getDimension().getType();
    }

    public ServerWorld getWorld() {
        return DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), gPos.getDimension(), false, false);
    }

    public IItemHandler getItemHandler() {
        ServerWorld w = getWorld();
        BlockPos pos = gPos.getPos();
        if (w == null || !w.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4))
            return null;
        TileEntity te = w.getTileEntity(pos);
        if (te == null) {
            return null;
        }
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).orElse(null);
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
        String s = blockTranslationKey == null || blockTranslationKey.isEmpty() ? "" : " [" + blockTranslationKey + "]";
        return MiscUtil.locToString(gPos.getDimension().getId(), gPos.getPos()) + " " + face + s;
    }

    public ITextComponent getTextComponent() {
        return new StringTextComponent(MiscUtil.locToString(gPos.getDimension().getId(), gPos.getPos()) + " " + face)
                .appendText(" [")
                .appendSibling(MiscUtil.xlate(blockTranslationKey))
                .appendText("]")
                .applyTextStyle(TextFormatting.AQUA);
    }
}
