package me.desht.modularrouters.item.module;

import com.google.common.collect.Maps;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.fx.Vector3;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ParticleBeamMessage;
import me.desht.modularrouters.sound.MRSoundEvents;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a module with a specific target block (blockpos stored in itemstack NBT).
 * Used by Mk2 & Mk3 senders, for example.
 */
public abstract class TargetedModule extends Module {
    private static final String NBT_TARGET = "Target";

    private static final Map<UUID,Long> lastSwing = Maps.newHashMap();

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        if (player.isSneaking()) {
                if (InventoryUtils.getInventory(world, pos, face) != null) {
                    if (world.isRemote) {
                        player.playSound(MRSoundEvents.success, 1.0f, 1.3f);
                    } else {
                        setTarget(stack, world, pos, face);
                        ModuleTarget tgt = getTarget(stack, true);
                        if (tgt != null) {
                            player.addChatMessage(new TextComponentTranslation("chatText.misc.targetSet", tgt.toString()));
                        }
                    }
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
            list.add(I18n.format("chatText.misc.target", target.toString()));
            if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
                TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
                TargetValidation val = validateTarget(router, target, false);
                if (val != TargetValidation.OK) {
                    list.add(I18n.format("chatText.targetValidation." + val));
                }
            }
        }
    }

    /**
     * Put information about the target into the module item's NBT.  This needs to be done server-side!
     *
     * @param stack the module item
     * @param world the world the target is in
     * @param pos the position of the target
     * @param face clicked face of the target
     */
    private static void setTarget(ItemStack stack, World world, BlockPos pos, EnumFacing face) {
        if (world.isRemote) {
            ModularRouters.logger.warn("TargetModule.setTarget() should not be called client-side!");
            return;
        }
        NBTTagCompound compound = ModuleHelper.validateNBT(stack);
        String invName = BlockUtil.getBlockName(world, pos);
        ModuleTarget mt = new ModuleTarget(world.provider.getDimension(), pos, face, invName == null ? "?" : invName);
        compound.setTag(NBT_TARGET, mt.toNBT());
        stack.setTagCompound(compound);
    }

    /**
     * Retrieve targeting information from a module itemstack.  Can be called server or client-side.
     *
     * @param stack the module item stack
     * @return targeting data
     */
    public static ModuleTarget getTarget(ItemStack stack) {
       return getTarget(stack, false);
    }

    /**
     * Retrieve targeting information from a module itemstack.  Can be called server or client-side; if called
     * server-side, it will also revalidate the name of the target block if the checkName parameter is true.
     *
     * @param stack the module item stack
     * @param checkName verify the name of the target block - only works server-side
     * @return targeting data
     */
    public static ModuleTarget getTarget(ItemStack stack, boolean checkName) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.getTagId(NBT_TARGET) == Constants.NBT.TAG_COMPOUND) {
            ModuleTarget target = ModuleTarget.fromNBT(compound.getCompoundTag(NBT_TARGET));
            if (checkName) {
                WorldServer w = DimensionManager.getWorld(target.dimId);
                if (w != null && w.getChunkProvider().chunkExists(target.pos.getX() >> 4, target.pos.getZ() >> 4)) {
                    String invName = BlockUtil.getBlockName(w, target.pos);
                    if (!target.invName.equals(invName)) {
                        setTarget(stack, w, target.pos, target.face);
                        return new ModuleTarget(target.dimId, target.pos, target.face, invName);
                    }
                }
            }
            return target;
        }
        return null;
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

        ModuleTarget src = new ModuleTarget(world.provider.getDimension(), player.getPosition());
        ModuleTarget target = getTarget(stack, true);
        if (target == null) {
            return false;
        }

        TargetValidation res = validateTarget(null, src, target, true);
        Vector3 orig = Vector3.fromEntityCenter(player);
        Vector3 end = Vector3.fromBlockPos(target.pos).add(0.5);
        if (src.dimId == target.dimId) {
            ModularRouters.network.sendTo(new ParticleBeamMessage(orig.x, orig.y, orig.z, end.x, end.y, end.z, null), player);
        }
        player.addChatMessage(new TextComponentTranslation("chatText.misc.target", target.toString())
                .appendText("  ")
                .appendSibling(new TextComponentTranslation("chatText.targetValidation." + res)));
        return true;
    }

    public abstract TargetValidation validateTarget(TileEntityItemRouter router, ModuleTarget src, ModuleTarget dst, boolean validateBlocks);

    private TargetValidation validateTarget(TileEntityItemRouter router, ModuleTarget dst, boolean validateBlocks) {
        return validateTarget(router, new ModuleTarget(router.getWorld().provider.getDimension(), router.getPos()), dst, validateBlocks);
    }

    enum TargetValidation {
        OK,
        OUT_OF_RANGE,
        NOT_LOADED,
        NOT_INVENTORY
    }
}
