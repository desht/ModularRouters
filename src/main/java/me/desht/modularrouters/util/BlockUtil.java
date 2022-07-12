package me.desht.modularrouters.util;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockUtil {
    private static final Direction[] HORIZONTALS = new Direction[] {
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private static BlockState getPlaceableState(BlockPlaceContext ctx) {
        try {
            BlockState res = null;
            Level world = ctx.getLevel();
            BlockPos pos = ctx.getClickedPos();
            Item item = ctx.getItemInHand().getItem();
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                res = block.getStateForPlacement(ctx);
            } else if (item instanceof IPlantable) {
                res = ((IPlantable) item).getPlant(world, pos);
            } else if (item == Items.COCOA_BEANS) {
                // special handling for cocoa bean planting
                res = getCocoaBeanState(ctx);
            }
            if (res != null && !res.canSurvive(world, pos)) {
                res = null;
            }
            return res;
        } catch (IllegalArgumentException e) {
            // See https://github.com/desht/ModularRouters/issues/25
            // Thanks Actually Additions for generating unplaceable random blocks, like double slabs :)
            return null;
        }
    }

    private static BlockState getCocoaBeanState(BlockPlaceContext ctx) {
        if (ctx.getPlayer() == null) return null;
        // try to find a jungle log in any horizontal direction
        for (Direction f : HORIZONTALS) {
            BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(f));
            if (state.getBlock() == Blocks.JUNGLE_LOG) {
                ctx.getPlayer().setYRot(getYawFromFacing(f));  // fake player must face the jungle log
                return Blocks.COCOA.getStateForPlacement(ctx);
            }
        }
        return null;
    }

    private static float getYawFromFacing(Direction facing) {
        return switch (facing) {
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f; // including SOUTH
        };
    }

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
    public static BlockState tryPlaceAsBlock(ModularRouterBlockEntity router, ItemStack toPlace, Level world, BlockPos pos, Direction facing) {
        BlockState currentState = world.getBlockState(pos);

        FakePlayer fakePlayer = router.getFakePlayer();
        fakePlayer.setYRot(getYawFromFacing(facing));
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, toPlace);

        float hitX = (float) (fakePlayer.getX() - pos.getX());
        float hitY = (float) (fakePlayer.getY() - pos.getY());
        float hitZ = (float) (fakePlayer.getZ() - pos.getZ());
        BlockHitResult brtr = new BlockHitResult(new Vec3(hitX, hitY, hitZ), facing, pos, false);
        BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(fakePlayer, InteractionHand.MAIN_HAND, brtr));
        if (!currentState.canBeReplaced(ctx)) {
            return null;
        }

        BlockState newState = getPlaceableState(ctx);
        if (newState != null) {
            BlockSnapshot snap = BlockSnapshot.create(world.dimension(), world, pos);
            fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, toPlace);
            BlockEvent.EntityPlaceEvent event = new CustomEntityPlaceEvent(snap, Blocks.AIR.defaultBlockState(), fakePlayer, newState);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && world.setBlockAndUpdate(pos, newState)) {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                BlockItem.updateCustomBlockEntityTag(world, fakePlayer, pos, toPlace);
                newState.getBlock().setPlacedBy(world, pos, newState, fakePlayer, toPlace);
                return newState;
            }
        }

        return null;
    }

    public static boolean tryPlaceBlock(ModularRouterBlockEntity router, BlockState newState, Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel) || !world.getBlockState(pos).getMaterial().isReplaceable()) return false;

        BlockSnapshot snap = BlockSnapshot.create(world.dimension(), world, pos);
        BlockEvent.EntityPlaceEvent event = new CustomEntityPlaceEvent(snap, Blocks.AIR.defaultBlockState(), router.getFakePlayer(), newState);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled() && world.setBlockAndUpdate(pos, newState);
    }

    /**
     * Try to break the block at the given position. If the block has any drops, but no drops pass the filter, then the
     * block will not be broken. Liquid, air & unbreakable blocks (bedrock etc.) will never be broken.  Drops will be
     * available via the BreakResult object, organised by whether they passed the filter.
     *
     * @param world     the world
     * @param pos       the block position
     * @param filter    filter for the block's drops
     * @param pickaxe   the pickaxe to use to break block
     * @return a drop result object
     */
    public static BreakResult tryBreakBlock(ModularRouterBlockEntity router, Level world, BlockPos pos, Filter filter, ItemStack pickaxe, boolean matchByBlock) {
        if (!(world instanceof ServerLevel serverWorld)) return BreakResult.NOT_BROKEN;

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (state.isAir() || state.getDestroySpeed(world, pos) < 0 || block instanceof LiquidBlock
                || matchByBlock && !filter.test(new ItemStack(block))) {
            return BreakResult.NOT_BROKEN;
        }

        FakePlayer fakePlayer = router.getFakePlayer();
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        if (ConfigHolder.common.module.breakerHarvestLevelLimit.get() && !ForgeHooks.isCorrectToolForDrops(state, fakePlayer)) {
            return BreakResult.NOT_BROKEN;
        }

        List<ItemStack> allDrops = Block.getDrops(world.getBlockState(pos), serverWorld, pos, world.getBlockEntity(pos), fakePlayer, pickaxe);
        Map<Boolean, List<ItemStack>> groups = allDrops.stream().collect(Collectors.partitioningBy(matchByBlock ? s -> true : filter));

        if (!matchByBlock && allDrops.isEmpty() && !filter.isEmpty() && !filter.isBlacklist()) {
            return BreakResult.NOT_BROKEN;
        }

        if (allDrops.isEmpty() || !groups.get(true).isEmpty()) {
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                world.removeBlock(pos, false);
                if (ConfigHolder.common.router.blockBreakXPDrops.get() && breakEvent.getExpToDrop() > 0) {
                    Vec3 vec = Vec3.atCenterOf(pos);
                    ExperienceOrb xpOrb = new ExperienceOrb(world, vec.x, vec.y, vec.z, breakEvent.getExpToDrop());
                    world.addFreshEntity(xpOrb);
                }
                return new BreakResult(true, groups);
            }
        }
        return BreakResult.NOT_BROKEN;
    }

    public static String getBlockName(Level w, BlockPos pos) {
        return w == null ? "" : w.getBlockState(pos).getBlock().getDescriptionId();
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class BreakResult {
        static final BreakResult NOT_BROKEN = new BreakResult(false, Collections.emptyMap());

        private final boolean blockBroken;
        private final Map<Boolean,List<ItemStack>> drops;

        BreakResult(boolean blockBroken, Map<Boolean, List<ItemStack>> drops) {
            this.blockBroken = blockBroken;
            this.drops = drops;
        }

        public boolean isBlockBroken() {
            return blockBroken;
        }

        List<ItemStack> getFilteredDrops(boolean passed) {
            return drops.getOrDefault(passed, Collections.emptyList());
        }

        /**
         * Process dropped items.  Items which matched the filter are inserted into the given item handler if possible.
         * Items which didn't match the filter, or which matched but could not be inserted, are dropped on the ground.
         *
         * @param world the world
         * @param pos the position to drop any items at
         * @param handler item handler to insert into
         */
        public void processDrops(Level world, BlockPos pos, IItemHandler handler) {
            for (ItemStack drop : getFilteredDrops(true)) {
                ItemStack excess = handler.insertItem(0, drop, false);
                if (!excess.isEmpty()) {
                    InventoryUtils.dropItems(world, Vec3.atCenterOf(pos), excess);
                }
            }
            for (ItemStack drop : getFilteredDrops(false)) {
                InventoryUtils.dropItems(world, Vec3.atCenterOf(pos), drop);
            }
        }
    }
}
