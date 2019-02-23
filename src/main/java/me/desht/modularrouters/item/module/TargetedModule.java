package me.desht.modularrouters.item.module;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.PlaySoundMessage;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a module with a specific target block or blocks (blockpos stored in itemstack NBT).
 * Used by Mk2 & Mk3 senders, for example.
 */
public abstract class TargetedModule extends ItemModule {
    private static final String NBT_TARGET = "Target";
    private static final String NBT_MULTI_TARGET = "MultiTarget";

    private static final Map<UUID,Long> lastSwing = Maps.newHashMap();

    public TargetedModule(Properties props) {
        super(props);
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext ctx) {
        if (ctx.getPlayer() != null && ctx.getPlayer().isSneaking()) {
            if (InventoryUtils.getInventory(ctx.getWorld(), ctx.getPos(), ctx.getFace()) != null) {
                if (getMaxTargets() == 1) {
                    handleSingleTarget(ctx.getItem(), ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getFace());
                } else {
                    handleMultiTarget(ctx.getItem(), ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getFace());
                }
                return EnumActionResult.SUCCESS;
            } else {
                return super.onItemUse(ctx);
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    private void handleMultiTarget(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face) {
        if (!world.isRemote) {
            boolean removing = false;
            String invName = BlockUtil.getBlockName(world, pos);
            world.getDimension().getType().getId();
            ModuleTarget tgt = new ModuleTarget(world.getDimension().getType().getId(), pos, face, invName);
            Set<ModuleTarget> targets = getTargets(stack, true);
            if (targets.contains(tgt)) {
                targets.remove(tgt);
                removing = true;
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.targetRemoved", tgt.toString(), targets.size(), getMaxTargets()), true);
            } else if (targets.size() < getMaxTargets()) {
                targets.add(tgt);
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.targetAdded", tgt.toString(), targets.size(), getMaxTargets()), true);
            } else {
                // too many targets already
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.tooManyTargets", getMaxTargets()), true);
                PlaySoundMessage.playSound(player, ObjectRegistry.SOUND_ERROR, 1.0f, 1.3f);
                return;
            }

            PlaySoundMessage.playSound(player, ObjectRegistry.SOUND_SUCCESS, 1.0f, removing ? 1.1f : 1.3f);
            setTargets(stack, targets);
        }
    }

    private void handleSingleTarget(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face) {
        if (!world.isRemote) {
            setTarget(stack, world, pos, face);
            ModuleTarget tgt = getTarget(stack, true);
            if (tgt != null) {
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.targetSet", tgt.toString()), true);
                PlaySoundMessage.playSound(player, ObjectRegistry.SOUND_SUCCESS, 1.0f, 1.3f);
            }
        }
    }


    @Override
    public void addUsageInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addUsageInformation(itemstack, list);
        list.add(new TextComponentTranslation(getMaxTargets() > 1 ? "itemText.targetingHintMulti" : "itemText.targetingHint"));
    }

    @Override
    protected void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        Set<ModuleTarget> targets;

        if (getMaxTargets() > 1) {
            targets = getTargets(itemstack, false);
        } else {
            targets = Sets.newHashSet(getTarget(itemstack));
        }

        for (ModuleTarget target : targets) {
            if (target != null) {
                list.add(new TextComponentTranslation("chatText.misc.target", target.toString()));
                if (Minecraft.getInstance().currentScreen instanceof GuiItemRouter) {
                    TileEntityItemRouter router = ((GuiItemRouter) Minecraft.getInstance().currentScreen).router;
                    ModuleTarget moduleTarget = new ModuleTarget(router.getWorld().dimension.getType().getId(), router.getPos());
                    TargetValidation val = validateTarget(itemstack, moduleTarget, target, false);
                    if (val != TargetValidation.OK) {
                        list.add(new TextComponentTranslation("chatText.targetValidation." + val));
                    }
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onSneakRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && getTarget(stack) != null && getMaxTargets() == 1) {
            setTarget(stack, world, null, null);
            PlaySoundMessage.playSound(player, ObjectRegistry.SOUND_SUCCESS, 1.0f, 1.1f);
            player.sendStatusMessage(new TextComponentTranslation("chatText.misc.targetCleared"), true);
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
            ModularRouters.LOGGER.warn("TargetModule.setTarget() should not be called client-side!");
            return;
        }
        NBTTagCompound compound = ModuleHelper.validateNBT(stack);
        if (pos == null) {
            compound.remove(NBT_TARGET);
        } else {
            String invName = BlockUtil.getBlockName(world, pos);
            ModuleTarget mt = new ModuleTarget(world.dimension.getType().getId(), pos, face, invName == null ? "?" : invName);
            compound.put(NBT_TARGET, mt.toNBT());
        }
        stack.setTag(compound);
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
        NBTTagCompound compound = stack.getTag();
        if (compound != null && compound.getTagId(NBT_TARGET) == Constants.NBT.TAG_COMPOUND) {
            ModuleTarget target = ModuleTarget.fromNBT(compound.getCompound(NBT_TARGET));
            if (checkBlockName) {
                ModuleTarget newTarget = updateTargetBlockName(stack, target);
                if (newTarget != null) return newTarget;
            }
            return target;
        }
        return null;
    }

    /**
     * Retrieve multi-targeting information from a module itemstack.
     *
     * @param stack the module item stack
     * @param checkBlockName verify the name of the target block - only works server-side
     * @return a list of targets for the module
     */
    public static Set<ModuleTarget> getTargets(ItemStack stack, boolean checkBlockName) {
        Set<ModuleTarget> result = Sets.newHashSet();

        NBTTagCompound compound = stack.getTag();
        if (compound != null && compound.getTagId(NBT_MULTI_TARGET) == Constants.NBT.TAG_LIST) {
            NBTTagList list = compound.getList(NBT_MULTI_TARGET, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ModuleTarget target = ModuleTarget.fromNBT(list.getCompound(i));
                if (checkBlockName) {
                    ModuleTarget newTarget = updateTargetBlockName(stack, target);
                    result.add(newTarget != null ? newTarget : target);
                } else {
                    result.add(target);
                }
            }
        }
        return result;
    }

    private static void setTargets(ItemStack stack, Set<ModuleTarget> targets) {
        NBTTagCompound compound = ModuleHelper.validateNBT(stack);
        NBTTagList list = new NBTTagList();
        for (ModuleTarget target : targets) {
            list.add(target.toNBT());
        }
        compound.put(NBT_MULTI_TARGET, list);
        stack.setTag(compound);
    }

    private static ModuleTarget updateTargetBlockName(ItemStack stack, ModuleTarget target) {
        WorldServer w = MiscUtil.getWorldForDimensionId(target.dimId);
        if (w != null && w.getChunkProvider().chunkExists(target.pos.getX() >> 4, target.pos.getZ() >> 4)) {
            String invName = BlockUtil.getBlockName(w, target.pos);
            if (!target.invName.equals(invName)) {
                setTarget(stack, w, target.pos, target.face);
                return new ModuleTarget(target.dimId, target.pos, target.face, invName);
            }
        }
        return null;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, EntityLivingBase entityLiving) {
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

        ModuleTarget src = new ModuleTarget(MiscUtil.getDimensionForWorld(world), player.getPosition());
        Set<ModuleTarget> targets = getMaxTargets() > 1 ?
                getTargets(stack, true) :
                Sets.newHashSet(getTarget(stack, true));
        for (ModuleTarget target : targets) {
            if (target != null) {
                TargetValidation res = validateTarget(stack, src, target, true);
                player.sendStatusMessage(new TextComponentTranslation("chatText.misc.target", target.toString())
                        .appendText("  ")
                        .appendSibling(new TextComponentTranslation("chatText.targetValidation." + res)), false);
            }
        }
        return true;
    }

    /**
     * Do some validation checks on the module's target.
     *
     * @param moduleStack the module's itemstack
     * @param src position and dimension of the module (could be a router or player)
     * @param dst position and dimension of the module's target
     * @param validateBlocks true if the destination block should be validated; loaded and holding an inventory
     * @return the validation result
     */
    private TargetValidation validateTarget(ItemStack moduleStack, ModuleTarget src, ModuleTarget dst, boolean validateBlocks) {
        if (isRangeLimited() && (src.dimId != dst.dimId || src.pos.distanceSq(dst.pos) > maxDistanceSq(moduleStack))) {
            return TargetValidation.OUT_OF_RANGE;
        }

        // validateBlocks will be true only when this is called server-side by left-clicking the module in hand,
        // or when the router is actually executing the module;
        // we can't reliably validate chunk loading or inventory presence on the client (for tooltip generation)
        if (validateBlocks) {
            WorldServer w = MiscUtil.getWorldForDimensionId(dst.dimId);
            if (w == null || !w.getChunkProvider().chunkExists(dst.pos.getX() >> 4, dst.pos.getZ() >> 4)) {
                return TargetValidation.NOT_LOADED;
            }
            if (w.getTileEntity(dst.pos) == null) {
                return TargetValidation.NOT_INVENTORY;
            }
        }
        return TargetValidation.OK;
    }

    private int maxDistanceSq(ItemStack stack) {
        if (stack.getItem() instanceof IRangedModule) {
            int r =  ((IRangedModule) stack.getItem()).getCurrentRange(stack);
            return r * r;
        }
        return 0;
    }

    protected int getMaxTargets() {
        return 1;
    }

    /**
     * Does this module have limited range?
     *
     * @return true if range is limited, false otherwise
     */
    protected boolean isRangeLimited() {
        return true;
    }

    enum TargetValidation {
        OK,
        OUT_OF_RANGE,
        NOT_LOADED,
        NOT_INVENTORY
    }
}
