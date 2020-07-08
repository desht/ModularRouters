package me.desht.modularrouters.item.module;

import com.google.common.collect.Sets;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Set;

import static me.desht.modularrouters.util.MiscUtil.asFormattable;
import static me.desht.modularrouters.util.MiscUtil.xlate;

/**
 * Represents a module with a specific target block or blocks (blockpos stored in itemstack NBT).
 */
public abstract class TargetedModule extends ItemModule {
    private static final String NBT_TARGET = "Target";
    private static final String NBT_MULTI_TARGET = "MultiTarget";

    TargetedModule(Item.Properties props) {
        super(props);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        if (ctx.getPlayer() != null && ctx.getPlayer().isCrouching()) {
            if (isValidTarget(ctx)) {
                if (getMaxTargets() == 1) {
                    handleSingleTarget(ctx.getItem(), ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getFace());
                } else {
                    handleMultiTarget(ctx.getItem(), ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getFace());
                }
                return ActionResultType.SUCCESS;
            } else {
                return super.onItemUse(ctx);
            }
        } else {
            return ActionResultType.PASS;
        }
    }

    protected boolean isValidTarget(ItemUseContext ctx) {
        return InventoryUtils.getInventory(ctx.getWorld(), ctx.getPos(), ctx.getFace()).isPresent();
    }

    private void handleSingleTarget(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction face) {
        if (!world.isRemote) {
            setTarget(stack, world, pos, face);
            ModuleTarget tgt = getTarget(stack, true);
            if (tgt != null) {
                IFormattableTextComponent msg = xlate("chatText.misc.targetSet").func_230529_a_(tgt.getTextComponent());
                player.sendStatusMessage(msg.func_240699_a_(TextFormatting.YELLOW), true);
                world.playSound(null, pos, ModSounds.SUCCESS.get(), SoundCategory.BLOCKS, 1.0f, 1.3f);
            }
        }
    }

    private void handleMultiTarget(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction face) {
        if (!world.isRemote) {
            boolean removing = false;
            String invName = BlockUtil.getBlockName(world, pos);
            GlobalPos gPos = MiscUtil.makeGlobalPos(world, pos);
            ModuleTarget tgt = new ModuleTarget(gPos, face, invName);
            Set<ModuleTarget> targets = getTargets(stack, true);
            if (targets.contains(tgt)) {
                targets.remove(tgt);
                removing = true;
                player.sendStatusMessage(xlate("chatText.misc.targetRemoved", targets.size(), getMaxTargets())
                        .func_230529_a_(tgt.getTextComponent()).func_240699_a_(TextFormatting.YELLOW), true);
            } else if (targets.size() < getMaxTargets()) {
                targets.add(tgt);
                player.sendStatusMessage(MiscUtil.xlate("chatText.misc.targetAdded", targets.size(), getMaxTargets())
                        .func_230529_a_(tgt.getTextComponent()).func_240699_a_(TextFormatting.YELLOW), true);
            } else {
                // too many targets already
                player.sendStatusMessage(MiscUtil.xlate("chatText.misc.tooManyTargets", getMaxTargets())
                        .func_240699_a_(TextFormatting.RED), true);
                world.playSound(null, pos, ModSounds.ERROR.get(), SoundCategory.BLOCKS, 1.0f, 1.3f);
                return;
            }

            world.playSound(null, pos, ModSounds.SUCCESS.get(), SoundCategory.BLOCKS, 1.0f, removing ? 1.1f : 1.3f);
            setTargets(stack, targets);
        }
    }


    @Override
    public void addUsageInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addUsageInformation(itemstack, list);
        MiscUtil.appendMultilineText(list, TextFormatting.YELLOW, getMaxTargets() > 1 ? "itemText.targetingHintMulti" : "itemText.targetingHint");
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
                ITextComponent msg = asFormattable(target.getTextComponent()).func_240699_a_(TextFormatting.WHITE);
//                ITextComponent msg = xlate("chatText.misc.target")
//                        .func_230529_a_(target.getTextComponent()).func_240699_a_(TextFormatting.YELLOW);
                list.add(msg);
                TileEntityItemRouter router = ClientUtil.getOpenItemRouter();
                if (router != null) {
                    ModuleTarget moduleTarget = new ModuleTarget(router.getGlobalPos());
                    TargetValidation val = validateTarget(itemstack, moduleTarget, target, false);
                    if (val != TargetValidation.OK) {
                        list.add(xlate(val.translationKey()).func_240699_a_(val.getColor()));
                    }
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onSneakRightClick(ItemStack stack, World world, PlayerEntity player, Hand hand) {
        if (!world.isRemote && getTarget(stack) != null && getMaxTargets() == 1) {
            setTarget(stack, world, null, null);
            world.playSound(null, new BlockPos(player.getPositionVec()), ModSounds.SUCCESS.get(), SoundCategory.BLOCKS, 1.0f, 1.1f);
            player.sendStatusMessage(xlate("chatText.misc.targetCleared").func_240699_a_(TextFormatting.YELLOW), true);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    /**
     * Put information about the target into the module item's NBT.  This needs to be done server-side!
     *
     * @param stack the module item
     * @param world the world the target is in
     * @param pos the position of the target
     * @param face clicked face of the target
     */
    private static void setTarget(ItemStack stack, World world, BlockPos pos, Direction face) {
        if (world.isRemote) {
            ModularRouters.LOGGER.warn("TargetModule.setTarget() should not be called client-side!");
            return;
        }
        CompoundNBT compound = ModuleHelper.validateNBT(stack);
        if (pos == null) {
            compound.remove(NBT_TARGET);
        } else {
            ModuleTarget mt = new ModuleTarget(MiscUtil.makeGlobalPos(world, pos), face, BlockUtil.getBlockName(world, pos));
            compound.put(NBT_TARGET, mt.toNBT());
        }
        stack.getTag().put(ModularRouters.MODID, compound);
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
        CompoundNBT compound = stack.getChildTag(ModularRouters.MODID);
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

        CompoundNBT compound = stack.getChildTag(ModularRouters.MODID);
        if (compound != null && compound.getTagId(NBT_MULTI_TARGET) == Constants.NBT.TAG_LIST) {
            ListNBT list = compound.getList(NBT_MULTI_TARGET, Constants.NBT.TAG_COMPOUND);
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
        CompoundNBT compound = ModuleHelper.validateNBT(stack);
        ListNBT list = new ListNBT();
        for (ModuleTarget target : targets) {
            list.add(target.toNBT());
        }
        compound.put(NBT_MULTI_TARGET, list);
        stack.getTag().put(ModularRouters.MODID, compound);
    }

    private static ModuleTarget updateTargetBlockName(ItemStack stack, ModuleTarget target) {
        ServerWorld w = MiscUtil.getWorldForGlobalPos(target.gPos);
        BlockPos pos = target.gPos.getPos();
        if (w != null && w.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4)) {
            String invName = BlockUtil.getBlockName(w, pos);
            if (!target.blockTranslationKey.equals(invName)) {
                setTarget(stack, w, pos, target.face);
                return new ModuleTarget(target.gPos, target.face, invName);
            }
        }
        return null;
    }

    /**
     * Called server-side from ValidateModuleMessage
     *
     * @param stack the module item
     * @param player the player holding the module
     */
    public void doModuleValidation(ItemStack stack, ServerPlayerEntity player) {
        ModuleTarget src = new ModuleTarget(MiscUtil.makeGlobalPos(player.getEntityWorld(), new BlockPos(player.getPositionVec())));
        Set<ModuleTarget> targets = getMaxTargets() > 1 ?
                getTargets(stack, true) :
                Sets.newHashSet(getTarget(stack, true));
        for (ModuleTarget target : targets) {
            if (target != null) {
                TargetValidation v = validateTarget(stack, src, target, true);
                IFormattableTextComponent msg = MiscUtil.asFormattable(target.getTextComponent())
                        .func_240702_b_(" ").func_230529_a_(xlate(v.translationKey()).func_240699_a_(v.getColor()));
                player.sendStatusMessage(msg, false);
            }
        }
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
        if (isRangeLimited() && (!src.isSameWorld(dst) || src.gPos.getPos().distanceSq(dst.gPos.getPos()) > maxDistanceSq(moduleStack))) {
            return TargetValidation.OUT_OF_RANGE;
        }

        // validateBlocks will be true only when this is called server-side by left-clicking the module in hand,
        // or when the router is actually executing the module;
        // we can't reliably validate chunk loading or inventory presence on the client (for tooltip generation)
        if (validateBlocks) {
            ServerWorld w = MiscUtil.getWorldForGlobalPos(dst.gPos);
            if (w == null || !w.getChunkProvider().chunkExists(dst.gPos.getPos().getX() >> 4, dst.gPos.getPos().getZ() >> 4)) {
                return TargetValidation.NOT_LOADED;
            }
            if (w.getTileEntity(dst.gPos.getPos()) == null) {
                return TargetValidation.NOT_INVENTORY;
            }
        }
        return TargetValidation.OK;
    }

    private int maxDistanceSq(ItemStack stack) {
        if (stack.getItem() instanceof IRangedModule) {
            int r = ((IRangedModule) stack.getItem()).getCurrentRange(stack);
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
        NOT_INVENTORY;

        TextFormatting getColor() {
            return this == OK ? TextFormatting.GREEN : TextFormatting.RED;
        }

        String translationKey() {
            return "chatText.targetValidation." + this.toString();
        }
    }
}
