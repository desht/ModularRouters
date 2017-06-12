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
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
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
                            player.sendStatusMessage(new TextComponentTranslation("chatText.misc.targetSet", tgt.toString()), false);
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
    protected void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addUsageInformation(itemstack, player, list, advanced);
        MiscUtil.appendMultiline(list, "itemText.targetingHint");
    }

    @Override
    protected void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);

        ModuleTarget target = getTarget(itemstack);
        if (target != null) {
            list.add(I18n.format("chatText.misc.target", target.toString()));
            if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
                TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
                ModuleTarget moduleTarget = new ModuleTarget(router.getWorld().provider.getDimension(), router.getPos());
                TargetValidation val = validateTarget(router, moduleTarget, target, false);
                if (val != TargetValidation.OK) {
                    list.add(I18n.format("chatText.targetValidation." + val));
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onSneakRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (getTarget(stack) != null) {
            if (world.isRemote) {
                player.playSound(MRSoundEvents.success, 1.0f, 1.3f);
            } else {
                setTarget(stack, world, null, null);
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.targetCleared"), false);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
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
        if (pos == null) {
            compound.removeTag(NBT_TARGET);
        } else {
            String invName = BlockUtil.getBlockName(world, pos);
            ModuleTarget mt = new ModuleTarget(world.provider.getDimension(), pos, face, invName == null ? "?" : invName);
            compound.setTag(NBT_TARGET, mt.toNBT());
        }
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
     * @param checkBlockName verify the name of the target block - only works server-side
     * @return targeting data
     */
    public static ModuleTarget getTarget(ItemStack stack, boolean checkBlockName) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null && compound.getTagId(NBT_TARGET) == Constants.NBT.TAG_COMPOUND) {
            ModuleTarget target = ModuleTarget.fromNBT(compound.getCompoundTag(NBT_TARGET));
            if (checkBlockName) {
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
            ModularRouters.network.sendTo(new ParticleBeamMessage(orig.x, orig.y, orig.z, end.x, end.y, end.z, null, 0.5f), player);
        }
        player.sendStatusMessage(new TextComponentTranslation("chatText.misc.target", target.toString())
                .appendText("  ")
                .appendSibling(new TextComponentTranslation("chatText.targetValidation." + res)), false);
        return true;
    }

    /**
     * Do some validation checks on the module's target.
     *
     * @param router item router the module is installed in (may be null)
     * @param src position and dimension of the module (could be a router or player)
     * @param dst position and dimension of the module's target
     * @param validateBlocks true if the destination block should be validated; loaded and holding an inventory
     * @return the validation result
     */
    protected TargetValidation validateTarget(TileEntityItemRouter router, ModuleTarget src, ModuleTarget dst, boolean validateBlocks) {
        if (isRangeLimited() && (src.dimId != dst.dimId || src.pos.distanceSq(dst.pos) > maxDistanceSq(router))) {
            return TargetValidation.OUT_OF_RANGE;
        }

        // validateBlocks will be true only when this is called server-side by left-clicking the module in hand,
        // or when the router is actually executing the module;
        // we can't reliably validate chunk loading or inventory presence on the client (for tooltip generation)
        if (validateBlocks) {
            WorldServer w = DimensionManager.getWorld(dst.dimId);
            if (w == null || !w.getChunkProvider().chunkExists(dst.pos.getX() >> 4, dst.pos.getZ() >> 4)) {
                return TargetValidation.NOT_LOADED;
            }
            if (w.getTileEntity(dst.pos) == null) {
                return TargetValidation.NOT_INVENTORY;
            }
        }
        return TargetValidation.OK;
    }

    /**
     * Does this module have limited range?
     *
     * @return true if range is limited, false otherwise
     */
    protected boolean isRangeLimited() {
        return true;
    }

    /**
     * Get the (square of) the maximum distance that this module can reach.
     *
     * @param router router the module is installed in (may be null)
     * @return
     */
    public abstract int maxDistanceSq(TileEntityItemRouter router);

    enum TargetValidation {
        OK,
        OUT_OF_RANGE,
        NOT_LOADED,
        NOT_INVENTORY
    }
}
