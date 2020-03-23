package me.desht.modularrouters.util;

import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockUtil {
    private static final Direction[] HORIZONTALS = new Direction[] {
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private static BlockState getPlaceableState(BlockItemUseContext ctx) {
        try {
            BlockState res = null;
            World world = ctx.getWorld();
            BlockPos pos = ctx.getPos();
            Item item = ctx.getItem().getItem();
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                res = block.getStateForPlacement(ctx);
            } else if (item instanceof IPlantable) {
                res = ((IPlantable) item).getPlant(world, pos);
            } else if (item == Items.COCOA_BEANS) {
                // special handling for cocoa bean planting
                res = getCocoaBeanState(ctx);
            }
            if (res != null && !res.isValidPosition(world, pos)) {
                res = null;
            }
            return res;
        } catch (IllegalArgumentException e) {
            // See https://github.com/desht/ModularRouters/issues/25
            // Thanks Actually Additions for generating unplaceable random blocks, like double slabs :)
            return null;
        }
    }

    private static BlockState getCocoaBeanState(BlockItemUseContext ctx) {
        // try to find a jungle log in any horizontal direction
        for (Direction f : HORIZONTALS) {
            BlockState state = ctx.getWorld().getBlockState(ctx.getPos().offset(f));
            if (state.getBlock() == Blocks.JUNGLE_LOG) {
                ctx.getPlayer().rotationYaw = getYawFromFacing(f);  // fake player must face the jungle log
                return Blocks.COCOA.getStateForPlacement(ctx);
            }
        }
        return null;
    }

    private static float getYawFromFacing(Direction facing) {
        switch (facing) {
            case WEST: return 90f;
            case NORTH: return 180f;
            case EAST: return 270f;
            case SOUTH: return 0f;
            default: return 0f; // shouldn't happen
        }
    }

    /**
     * Try to place the given item as a block in the world.  This will fail if the block currently at the
     * placement position isn't replaceable, or world physics disallows the new block from being placed.
     *
     * @param toPlace item to place
     * @param world   the world
     * @param pos     position in the world to place at
     * @param facing direction the placer is facing
     * @param horizFacing fallback direction if the block to be placed only supports horizontal rotations
     * @return the new block state if successful, null otherwise
     */
    public static BlockState tryPlaceAsBlock(ItemStack toPlace, World world, BlockPos pos, Direction facing, Direction horizFacing) {
        BlockState currentState = world.getBlockState(pos);

        FakePlayer fakePlayer = FakePlayerManager.getFakePlayer((ServerWorld) world, pos);
        if (fakePlayer == null) {
            return null;
        }
        fakePlayer.rotationYaw = getYawFromFacing(facing);
        fakePlayer.setHeldItem(Hand.MAIN_HAND, toPlace);

        float hitX = (float) (fakePlayer.getPosX() - pos.getX());
        float hitY = (float) (fakePlayer.getPosY() - pos.getY());
        float hitZ = (float) (fakePlayer.getPosZ() - pos.getZ());
        BlockRayTraceResult brtr = new BlockRayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos, false);
        BlockItemUseContext ctx = new BlockItemUseContext(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, brtr));
        if (!currentState.isReplaceable(ctx)) {
            return null;
        }

        BlockState newState = getPlaceableState(ctx);
        if (newState != null) {
            BlockSnapshot snap = new BlockSnapshot(world, pos, newState);
            fakePlayer.setHeldItem(Hand.MAIN_HAND, toPlace);
            BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(snap, Blocks.AIR.getDefaultState(), fakePlayer);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && world.setBlockState(pos, newState, 3)) {
                fakePlayer.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                BlockItem.setTileEntityNBT(world, fakePlayer, pos, toPlace);
                newState.getBlock().onBlockPlacedBy(world, pos, newState, fakePlayer, toPlace);
                return newState;
            }
        }

        return null;
    }

    /**
     * Try to break the block at the given position. If the block has any drops, but no drops pass the filter, then the
     * block will not be broken. Liquid, air & unbreakable blocks (bedrock etc.) will never be broken.  Drops will be
     * available via the BreakResult object, organised by whether or not they passed the filter.
     *
     * @param world     the world
     * @param pos       the block position
     * @param filter    filter for the block's drops
     * @param silkTouch use silk touch when breaking the block
     * @param fortune   use fortune when breaking the block
     * @return a drop result object
     */
    public static BreakResult tryBreakBlock(World world, BlockPos pos, Filter filter, boolean silkTouch, int fortune) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block.isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0 || block instanceof FlowingFluidBlock) {
            return BreakResult.NOT_BROKEN;
        }

        FakePlayer fakePlayer = FakePlayerManager.getFakePlayer((ServerWorld) world, pos);
        List<ItemStack> allDrops = getDrops(world, pos, fakePlayer, silkTouch, fortune);

        Map<Boolean, List<ItemStack>> groups = allDrops.stream().collect(Collectors.partitioningBy(filter));
        if (allDrops.isEmpty() || !groups.get(true).isEmpty()) {
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                world.removeBlock(pos, false);
                return new BreakResult(true, groups);
            }
        }
        return BreakResult.NOT_BROKEN;
    }

    private static List<ItemStack> getDrops(World world, BlockPos pos, PlayerEntity player, boolean silkTouch, int fortune) {
        BlockState state = world.getBlockState(pos);

        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        if (fortune > 0) {
            pick.addEnchantment(Enchantments.FORTUNE, fortune);
        } else if (silkTouch) {
            pick.addEnchantment(Enchantments.SILK_TOUCH, 1);
        }
        LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                .withParameter(LootParameters.POSITION, pos)
                .withParameter(LootParameters.BLOCK_STATE, state)
                .withParameter(LootParameters.TOOL, pick)
                .withParameter(LootParameters.THIS_ENTITY, player);
        TileEntity te = world.getTileEntity(pos);
        if (te != null) builder = builder.withParameter(LootParameters.BLOCK_ENTITY, te);
        List<ItemStack> drops = state.getDrops(builder);
        NonNullList<ItemStack> dropsN = NonNullList.create();
        dropsN.addAll(drops);
        float dropChance = ForgeEventFactory.fireBlockHarvesting(dropsN, world, pos, state, fortune, 1.0F, silkTouch, player);
        return drops.stream().filter(s -> world.rand.nextFloat() <= dropChance).collect(Collectors.toList());
    }

    public static String getBlockName(World w, BlockPos pos) {
        return w == null ? "" : w.getBlockState(pos).getBlock().getTranslationKey();
    }

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
        public void processDrops(World world, BlockPos pos, IItemHandler handler) {
            for (ItemStack drop : getFilteredDrops(true)) {
                ItemStack excess = handler.insertItem(0, drop, false);
                if (!excess.isEmpty()) {
                    InventoryUtils.dropItems(world, pos, excess);
                }
            }
            for (ItemStack drop : getFilteredDrops(false)) {
                InventoryUtils.dropItems(world, pos, drop);
            }
        }
    }
}
