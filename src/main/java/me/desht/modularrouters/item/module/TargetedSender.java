package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/**
 * Represents a sender module with a specific target block.  Used by Mk2 & Mk3 senders.
 */
public abstract class TargetedSender extends AbstractModule {
    TargetedSender(String name) {
        super(name);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (player.isSneaking()) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te.hasCapability(ModularRouters.ITEM_HANDLER_CAPABILITY, null)) {
                setTarget(stack, world, pos, face);
                player.addChatMessage(new TextComponentTranslation("chatText.misc.targetSet", MiscUtil.locToString(world, pos)));
                return EnumActionResult.SUCCESS;
            } else {
                return super.onItemUse(stack, player, world, pos, hand, face, x, y, z);
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    static void setTarget(ItemStack stack, World world, BlockPos pos, EnumFacing face) {
        validateNBT(stack);
        NBTTagCompound compound = stack.getTagCompound();
        NBTTagCompound target = new NBTTagCompound();
        target.setInteger("Dimension", world.provider.getDimension());
        target.setInteger("X", pos.getX());
        target.setInteger("Y", pos.getY());
        target.setInteger("Z", pos.getZ());
        target.setInteger("Face", face.ordinal());
        compound.setTag("Target", target);
        stack.setTagCompound(compound);
    }

    public static DimensionPos getTarget(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey("Target")) {
            NBTTagCompound target = compound.getCompoundTag("Target");
            int dimId = target.getInteger("Dimension");
            int x = target.getInteger("X");
            int y = target.getInteger("Y");
            int z = target.getInteger("Z");
            EnumFacing face = EnumFacing.values()[target.getInteger("Face")];
            return new DimensionPos(dimId, x, y, z, face);
        } else {
            return null;
        }
    }

    public static class DimensionPos {
        public final int dimId;
        public final BlockPos pos;
        public final EnumFacing face;

        public DimensionPos(int dimId, int x, int y, int z, EnumFacing face) {
            this.dimId = dimId;
            this.pos = new BlockPos(x, y, z);
            this.face = face;
        }
    }
}
