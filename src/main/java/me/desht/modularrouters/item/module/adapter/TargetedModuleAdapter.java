package me.desht.modularrouters.item.module.adapter;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModSounds;
import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.ITargetedModule;
import me.desht.modularrouters.item.module.TargetValidation;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.asMutableComponent;

public record TargetedModuleAdapter(ITargetedModule targeted) implements IItemAdapter {
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()) {
            if (targeted.getMaxTargets() == 1) {
                if (ITargetedModule.canSelectTarget(ctx)) {
                    handleSingleTarget(ctx.getItemInHand(), ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace());
                    return InteractionResult.SUCCESS;
                }
            } else {
                return handleMultiTarget(ctx.getItemInHand(), ctx, ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> onSneakRightClick(ItemStack stack, Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide && !ITargetedModule.getTargets(stack, false).isEmpty() && targeted.getMaxTargets() == 1) {
            ITargetedModule.setTargets(stack, Set.of());
            world.playSound(null, player.blockPosition(), ModSounds.SUCCESS.get(), SoundSource.BLOCKS,
                    ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.1f);
            player.displayClientMessage(Component.translatable("modularrouters.chatText.misc.targetCleared").withStyle(ChatFormatting.YELLOW), true);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private void handleSingleTarget(ItemStack stack, Player player, Level world, BlockPos pos, Direction face) {
        if (!world.isClientSide) {
            ITargetedModule.setTargets(stack, Set.of(new ModuleTarget(world, pos, face)));
            var tgts = ITargetedModule.getTargets(stack, true);
            if (!tgts.isEmpty()) {
                MutableComponent msg = Component.translatable("modularrouters.chatText.misc.targetSet").append(tgts.iterator().next().getTextComponent());
                player.displayClientMessage(msg.withStyle(ChatFormatting.YELLOW), true);
                world.playSound(null, pos, ModSounds.SUCCESS.get(), SoundSource.BLOCKS,
                        ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.3f);
            }
        }
    }

    private InteractionResult handleMultiTarget(ItemStack stack, UseOnContext context, Player player, Level world, BlockPos pos, Direction face) {
        Set<ModuleTarget> targets = ITargetedModule.getTargets(stack, !world.isClientSide);
        String invName = BlockUtil.getBlockName(world, pos);
        GlobalPos gPos = MiscUtil.makeGlobalPos(world, pos);
        ModuleTarget tgt = new ModuleTarget(gPos, face, invName);

        // Allow removing targets without checking if they're valid
        if (targets.contains(tgt)) {
            if (world.isClientSide) return InteractionResult.SUCCESS;
            targets.remove(tgt);

            player.displayClientMessage(Component.translatable("modularrouters.chatText.misc.targetRemoved", targets.size(), targeted.getMaxTargets())
                    .append(tgt.getTextComponent()).withStyle(ChatFormatting.YELLOW), true);
            world.playSound(null, pos, ModSounds.SUCCESS.get(), SoundSource.BLOCKS, ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.1f);
            ITargetedModule.setTargets(stack, targets);
            return InteractionResult.SUCCESS;
        }

        if (ITargetedModule.canSelectTarget(context)) {
            if (world.isClientSide) return InteractionResult.SUCCESS;
            if (targets.size() < targeted.getMaxTargets()) {
                targets.add(tgt);
                player.displayClientMessage(Component.translatable("modularrouters.chatText.misc.targetAdded", targets.size(), targeted.getMaxTargets())
                        .append(tgt.getTextComponent()).withStyle(ChatFormatting.YELLOW), true);

                world.playSound(null, pos, ModSounds.SUCCESS.get(), SoundSource.BLOCKS,
                        ConfigHolder.common.sound.bleepVolume.get().floatValue(), 1.3f);
                ITargetedModule.setTargets(stack, targets);
            } else {
                // too many targets already
                player.displayClientMessage(Component.translatable("modularrouters.chatText.misc.tooManyTargets", targeted.getMaxTargets())
                        .withStyle(ChatFormatting.RED), true);
                world.playSound(null, pos, ModSounds.ERROR.get(), SoundSource.BLOCKS, 1.0f, 1.3f);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addSettingsInformation(ItemStack stack, List<Component> list) {
        Set<ModuleTarget> targets = ITargetedModule.getTargets(stack, false);

        for (ModuleTarget target : targets) {
            if (target != null) {
                Component msg = Component.literal("▶ ").append(asMutableComponent(target.getTextComponent()).withStyle(ChatFormatting.WHITE));
                list.add(msg);
                ClientUtil.getOpenItemRouter().ifPresent(router -> {
                    ModuleTarget moduleTarget = new ModuleTarget(router.getGlobalPos());
                    TargetValidation val = validateTarget(stack, moduleTarget, target, false);
                    if (val != TargetValidation.OK) {
                        list.add(xlate(val.translationKey()).withStyle(val.getColor()));
                    }
                });
            }
        }
    }

    @Override
    public void doModuleValidation(ItemStack stack, ServerPlayer player) {
        ModuleTarget src = new ModuleTarget(MiscUtil.makeGlobalPos(player.getCommandSenderWorld(), player.blockPosition()));
        Set<ModuleTarget> targets = ITargetedModule.getTargets(stack, true);
        for (ModuleTarget target : targets) {
            if (target != null) {
                TargetValidation v = validateTarget(stack, src, target, true);
                MutableComponent msg = MiscUtil.asMutableComponent(target.getTextComponent())
                        .append(" ").append(Component.translatable(v.translationKey()).withStyle(v.getColor()));
                player.displayClientMessage(msg, false);
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
        if (targeted.isRangeLimited() && (!src.isSameWorld(dst) || src.gPos.pos().distSqr(dst.gPos.pos()) > maxDistanceSq(moduleStack))) {
            return TargetValidation.OUT_OF_RANGE;
        }

        // validateBlocks will be true only when this is called server-side by left-clicking the module in hand,
        // or when the router is actually executing the module;
        // we can't reliably validate chunk loading or inventory presence on the client (for tooltip generation)
        if (validateBlocks) {
            ServerLevel w = MiscUtil.getWorldForGlobalPos(dst.gPos);
            if (w == null || !w.getChunkSource().hasChunk(dst.gPos.pos().getX() >> 4, dst.gPos.pos().getZ() >> 4)) {
                return TargetValidation.NOT_LOADED;
            }
            if (w.getBlockEntity(dst.gPos.pos()) == null) {
                return TargetValidation.NOT_INVENTORY;
            }
            if (!targeted.canOperateInDimension(dst.gPos.dimension()) || !targeted.canOperateInDimension(src.gPos.dimension())) {
                return TargetValidation.BAD_DIMENSION;
            }
        }
        return TargetValidation.OK;
    }

    private int maxDistanceSq(ItemStack stack) {
        if (stack.getItem() instanceof IRangedModule rangedModule) {
            int r = rangedModule.getCurrentRange(stack);
            return r * r;
        }
        return 0;
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<Component> list) {
        list.add(xlate(targeted.getMaxTargets() > 1 ? "modularrouters.itemText.targetingHintMulti" : "modularrouters.itemText.targetingHint").withStyle(ChatFormatting.YELLOW));
    }
}
