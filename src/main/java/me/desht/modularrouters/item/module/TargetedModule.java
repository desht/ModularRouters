package me.desht.modularrouters.item.module;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.PlaySoundMessage;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static me.desht.modularrouters.util.MiscUtil.xlate;

/**
 * Represents a module with a specific target block or blocks (blockpos stored in itemstack NBT).
 */
public abstract class TargetedModule extends ItemModule {
    private static final String NBT_TARGET = "Target";
    private static final String NBT_MULTI_TARGET = "MultiTarget";

    private static final Map<UUID,Long> lastSwing = Maps.newHashMap();

    TargetedModule(Item.Properties props) {
        super(props);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        if (ctx.getPlayer() != null && ctx.getPlayer().isSteppingCarefully()) {
            return InventoryUtils.getInventory(ctx.getWorld(), ctx.getPos(), ctx.getFace()).map(handler -> {
                if (getMaxTargets() == 1) {
                    handleSingleTarget(ctx.getItem(), ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getFace());
                } else {
                    handleMultiTarget(ctx.getItem(), ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getFace());
                }
                return ActionResultType.SUCCESS;
            }).orElse(super.onItemUse(ctx));
        } else {
            return ActionResultType.PASS;
        }
    }

    private void handleSingleTarget(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction face) {
        if (!world.isRemote) {
            setTarget(stack, world, pos, face);
            ModuleTarget tgt = getTarget(stack, true);
            if (tgt != null) {
                ITextComponent msg = xlate("chatText.misc.targetSet").appendSibling(tgt.getTextComponent());
                player.sendStatusMessage(msg.applyTextStyle(TextFormatting.YELLOW), true);
                PlaySoundMessage.playSound(player, ModSounds.SUCCESS.get(), 1.0f, 1.3f);
            }
        }
    }

    private void handleMultiTarget(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction face) {
        if (!world.isRemote) {
            boolean removing = false;
            String invName = BlockUtil.getBlockName(world, pos);
            GlobalPos gPos = GlobalPos.of(world.getDimension().getType(), pos);
            ModuleTarget tgt = new ModuleTarget(gPos, face, invName);
            Set<ModuleTarget> targets = getTargets(stack, true);
            if (targets.contains(tgt)) {
                targets.remove(tgt);
                removing = true;
                player.sendStatusMessage(xlate("chatText.misc.targetRemoved", targets.size(), getMaxTargets())
                        .appendSibling(tgt.getTextComponent()).applyTextStyle(TextFormatting.YELLOW), true);
            } else if (targets.size() < getMaxTargets()) {
                targets.add(tgt);
                player.sendStatusMessage(new TranslationTextComponent("chatText.misc.targetAdded", targets.size(), getMaxTargets())
                        .appendSibling(tgt.getTextComponent()).applyTextStyle(TextFormatting.YELLOW), true);
            } else {
                // too many targets already
                player.sendStatusMessage(new TranslationTextComponent("chatText.misc.tooManyTargets", getMaxTargets())
                        .applyTextStyle(TextFormatting.RED), true);
                PlaySoundMessage.playSound(player, ModSounds.ERROR.get(), 1.0f, 1.3f);
                return;
            }

            PlaySoundMessage.playSound(player, ModSounds.SUCCESS.get(), 1.0f, removing ? 1.1f : 1.3f);
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
                ITextComponent msg = xlate("chatText.misc.target")
                        .appendSibling(target.getTextComponent()).applyTextStyle(TextFormatting.YELLOW);
                list.add(msg);
                TileEntityItemRouter router = ClientUtil.getOpenItemRouter();
                if (router != null) {
                    ModuleTarget moduleTarget = new ModuleTarget(router.getGlobalPos());
                    TargetValidation val = validateTarget(itemstack, moduleTarget, target, false);
                    if (val != TargetValidation.OK) {
                        list.add(xlate(val.translationKey()).applyTextStyle(val.getColor()));
                    }
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onSneakRightClick(ItemStack stack, World world, PlayerEntity player, Hand hand) {
        if (!world.isRemote && getTarget(stack) != null && getMaxTargets() == 1) {
            setTarget(stack, world, null, null);
            PlaySoundMessage.playSound(player, ModSounds.SUCCESS.get(), 1.0f, 1.1f);
            player.sendStatusMessage(xlate("chatText.misc.targetCleared").applyTextStyle(TextFormatting.YELLOW), true);
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
            GlobalPos gPos = GlobalPos.of(world.getDimension().getType(), pos);
            ModuleTarget mt = new ModuleTarget(gPos, face, BlockUtil.getBlockName(world, pos));
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

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entityLiving) {
        if (!(entityLiving instanceof ServerPlayerEntity)) {
            return false;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) entityLiving;
        World world = player.getEntityWorld();

        // prevent message spamming
        long now = System.currentTimeMillis();
        if (now - lastSwing.getOrDefault(player.getUniqueID(), 0L) < 250) {
            return true;
        }
        lastSwing.put(player.getUniqueID(), now);

        ModuleTarget src = new ModuleTarget(GlobalPos.of(world.getDimension().getType(), player.getPosition()));
        Set<ModuleTarget> targets = getMaxTargets() > 1 ?
                getTargets(stack, true) :
                Sets.newHashSet(getTarget(stack, true));
        for (ModuleTarget target : targets) {
            if (target != null) {
                TargetValidation v = validateTarget(stack, src, target, true);
                ITextComponent msg = xlate("chatText.misc.target").appendSibling(target.getTextComponent());
                msg.appendText(" ");
                msg.appendSibling(xlate(v.translationKey()).applyTextStyle(v.getColor()));
                msg.applyTextStyle(TextFormatting.YELLOW);
                player.sendStatusMessage(msg, false);
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
        if (isRangeLimited() && (src.gPos.getDimension() != dst.gPos.getDimension() || src.gPos.getPos().distanceSq(dst.gPos.getPos()) > maxDistanceSq(moduleStack))) {
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
