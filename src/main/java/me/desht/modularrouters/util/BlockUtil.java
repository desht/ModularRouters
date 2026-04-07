package me.desht.modularrouters.util;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockUtil {
    /**
     * Try to place the given item as a block in the world.  This will fail if the block currently at the
     * placement position isn't replaceable, or world physics disallows the new block from being placed.
     *
     * @param toPlace item to place
     * @param world   the world
     * @param pos     position in the world to place at
     * @param facing direction the placer is facing
     * @return the new block state if successful, null otherwise
     */
    @Nullable
    public static BlockState tryPlaceAsBlock(ModularRouterBlockEntity router, ItemStack toPlace, Level world, BlockPos pos, Direction facing) {
        if (!(toPlace.getItem() instanceof BlockItem)) {
            return null;
        }

        BlockState currentState = world.getBlockState(pos);

        FakePlayer fakePlayer = router.getFakePlayer();
        fakePlayer.setYRot(getYawFromFacing(facing));
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, toPlace);

        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), facing, pos, false);
        UseOnContext useCtx = new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, hitResult);

        if (!currentState.canBeReplaced(new BlockPlaceContext(useCtx))) {
            return null;
        }

        try {
            InteractionResult res = toPlace.useOn(useCtx);
            return res instanceof InteractionResult.Success s && s.wasItemInteraction() ? world.getBlockState(pos) : null;
        } catch (Exception ignored) {
            // just in case some modded item has flawed placement logic...
            return null;
        }
    }

    public static boolean tryPlaceBlock(ModularRouterBlockEntity router, BlockState newState, Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel) || !world.getBlockState(pos).canBeReplaced()) return false;

        BlockSnapshot snap = BlockSnapshot.create(world.dimension(), world, pos);
        BlockEvent.EntityPlaceEvent event = new CustomEntityPlaceEvent(snap, Blocks.AIR.defaultBlockState(), router.getFakePlayer(), newState);
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled() && world.setBlockAndUpdate(pos, newState);
    }

    /**
     * Try to break the block at the given position. If the block has any drops, but no drops pass the filter, then the
     * block will not be broken. Liquid, air & unbreakable blocks (bedrock etc.) will never be broken.  Drops will be
     * available via the BreakResult object, organised by whether they passed the filter.
     *
     * @param router    the router whose fake player will be doing the block breaking
     * @param world     the world
     * @param pos       the block position
     * @param filter    filter for the block's drops
     * @param pickaxe   the pickaxe being used by the fake player to break block
     * @return true if the block was broken, false otherwise
     */
    public static boolean tryBreakBlock(ModularRouterBlockEntity router, Level world, BlockPos pos, Filter filter, ItemStack pickaxe, boolean matchByBlock) {
        if (!(world instanceof ServerLevel serverWorld)) return false;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (state.isAir() || state.getDestroySpeed(world, pos) < 0 || block instanceof LiquidBlock
                || matchByBlock && !filter.test(new ItemStack(block))) {
            return false;
        }

        if (ConfigHolder.common.module.breakerHarvestLevelLimit.get() && state.requiresCorrectToolForDrops()) {
            Tool tool = pickaxe.get(DataComponents.TOOL);
            if (tool == null || !tool.isCorrectForDrops(state)) {
                return false;
            }
        }

        FakePlayer fakePlayer = router.getFakePlayer();
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);

        List<ItemStack> allDrops = Block.getDrops(world.getBlockState(pos), serverWorld, pos, world.getBlockEntity(pos), fakePlayer, pickaxe);
        Map<Boolean, List<ItemStack>> groups = allDrops.stream().collect(Collectors.partitioningBy(matchByBlock ? s -> true : filter));

        if (!matchByBlock && allDrops.isEmpty() && !filter.isEmpty() && filter.isWhiteList()) {
            return false;
        }

        // See also MiscEventHandler#onBlockDrops for handling of dropped items
        return (allDrops.isEmpty() || !groups.get(true).isEmpty()) && fakePlayer.gameMode.destroyBlock(pos);
    }

    public static String getBlockName(@Nullable Level level, BlockPos pos) {
        return level == null || !level.isLoaded(pos) ? "" : level.getBlockState(pos).getBlock().getDescriptionId();
    }

    private static float getYawFromFacing(Direction facing) {
        return switch (facing) {
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f; // including SOUTH
        };
    }
}
