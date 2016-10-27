package me.desht.modularrouters.item.module;

import com.google.common.collect.Maps;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.fx.Vector3;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ParticleBeamMessage;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a module with a specific target block (blockpos stored in itemstack NBT).
 * Used by Mk2 & Mk3 senders.
 */
public abstract class TargetedModule extends Module {
    private static final String NBT_TARGET = "Target";

    private static final Map<UUID,Long> lastSwing = Maps.newHashMap();

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (player.isSneaking()) {
            if (InventoryUtils.getInventory(world, pos, face) != null) {
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

    @Override
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(stack, player, list, par4);

        ModuleTarget target = getTarget(stack);
        if (target != null) {
            list.add(I18n.format("itemText.misc.target", target.dimId, target.pos.getX(), target.pos.getY(), target.pos.getZ(), target.face.getName()));
            if (target.dimId == player.getEntityWorld().provider.getDimension()) {
                String name = getBlockName(ModularRouters.proxy.theClientWorld(), target.pos);
                if (name != null) {
                    list.add("        (" + name + ")");
                }
            }
            if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
                TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
                TargetValidation val = validateTarget(router, target, false);
                if (val != TargetValidation.OK) {
                    list.add(I18n.format("itemText.targetValidation." + val));
                }
            }
        }
    }

    private String getBlockName(World w, BlockPos pos) {
        if (w == null) {
            return null;
        }
        IBlockState state = w.getBlockState(pos);
        if (state.getBlock().isAir(state, w, pos)) {
            return null;
        } else {
            ItemStack stack = state.getBlock().getItem(w, pos, state);
            if (stack != null) {
                return stack.getDisplayName();
            } else {
                return state.getBlock().getLocalizedName();
            }
        }
    }

    private static void setTarget(ItemStack stack, World world, BlockPos pos, EnumFacing face) {
        NBTTagCompound compound = validateNBT(stack);
        NBTTagCompound target = new NBTTagCompound();
        target.setInteger("Dimension", world.provider.getDimension());
        target.setInteger("X", pos.getX());
        target.setInteger("Y", pos.getY());
        target.setInteger("Z", pos.getZ());
        target.setInteger("Face", face.ordinal());
        compound.setTag(NBT_TARGET, target);
        stack.setTagCompound(compound);
    }

    public static ModuleTarget getTarget(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.hasKey(NBT_TARGET)) {
            NBTTagCompound target = compound.getCompoundTag(NBT_TARGET);
            int dimId = target.getInteger("Dimension");
            int x = target.getInteger("X");
            int y = target.getInteger("Y");
            int z = target.getInteger("Z");
            EnumFacing face = EnumFacing.values()[target.getInteger("Face")];
            return new ModuleTarget(dimId, x, y, z, face);
        } else {
            return null;
        }
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (!(entityLiving instanceof EntityPlayerMP)) {
            return false;
        }
        EntityPlayerMP player = (EntityPlayerMP) entityLiving;
        World world = player.getEntityWorld();
        if (world.isRemote) {
            return true;
        }
        if (player.isSneaking()) {
            return false;
        }

        // prevent message spamming
        long now = System.currentTimeMillis();
        if (now - lastSwing.getOrDefault(player.getUniqueID(), 0L) < 250) {
            return true;
        }
        lastSwing.put(player.getUniqueID(), now);

        ModuleTarget src = new ModuleTarget(world.provider.getDimension(), player.getPosition(), null);
        ModuleTarget target = getTarget(stack);
        if (target == null) {
            return false;
        }
        TargetValidation res = validateTarget(null, src, target, true);
        Vector3 orig = Vector3.fromEntityCenter(player);
        Vector3 end = Vector3.fromBlockPos(target.pos).add(0.5);
        if (src.dimId == target.dimId) {
            ModularRouters.network.sendTo(new ParticleBeamMessage(orig.x, orig.y, orig.z, end.x, end.y, end.z, null), player);
        }
        player.addChatMessage(new TextComponentTranslation("itemText.misc.target", target.dimId, target.pos.getX(), target.pos.getY(), target.pos.getZ(), target.face.getName())
                .appendText("  ")
                .appendSibling(new TextComponentTranslation("itemText.targetValidation." + res)));
        return true;
    }

    public abstract TargetValidation validateTarget(TileEntityItemRouter router, ModuleTarget src, ModuleTarget dst, boolean validateBlocks);

    private TargetValidation validateTarget(TileEntityItemRouter router, ModuleTarget dst, boolean validateBlocks) {
        return validateTarget(router, new ModuleTarget(router.getWorld().provider.getDimension(), router.getPos(), null), dst, validateBlocks);
    }

    enum TargetValidation {
        OK,
        OUT_OF_RANGE,
        NOT_LOADED,
        NOT_INVENTORY
    }
}
