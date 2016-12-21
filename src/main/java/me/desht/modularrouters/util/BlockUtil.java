package me.desht.modularrouters.util;

import com.google.common.collect.Lists;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockUtil {
    private static final String[] REED_ITEM = new String[]{"block", "field_150935_a", "a"};

    private static IBlockState getPlaceableState(ItemStack stack, World world, BlockPos pos) {
        // With thanks to Vazkii for inspiration from the Rannuncarpus code :)
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).block.getStateFromMeta(item.getMetadata(stack.getItemDamage()));
        } else if (item instanceof ItemBlockSpecial) {
            return ((Block) ReflectionHelper.getPrivateValue(ItemBlockSpecial.class, (ItemBlockSpecial) item, REED_ITEM)).getDefaultState();
        } else if (item instanceof ItemRedstone) {
            return Blocks.REDSTONE_WIRE.getDefaultState();
        } else if (item instanceof IPlantable) {
            IBlockState state = ((IPlantable) item).getPlant(world, pos);
            return ((state.getBlock() instanceof BlockCrops) && ((BlockCrops) state.getBlock()).canBlockStay(world, pos, state)) ? state : null;
        } else {
            return null;
        }
    }

    /**
     * Try to place the given item as a block in the world.
     *
     * @param toPlace item to place
     * @param world   the world
     * @param pos     position in the world to place at
     * @return the new block state if successful, null otherwise
     */
    public static IBlockState tryPlaceAsBlock(ItemStack toPlace, World world, BlockPos pos) {
        IBlockState currentState = world.getBlockState(pos);
        if (!currentState.getBlock().isAir(currentState, world, pos) || !currentState.getBlock().isReplaceable(world, pos)) {
            return null;
        }

        IBlockState newState = getPlaceableState(toPlace, world, pos);
        if (newState != null && newState.getBlock().canPlaceBlockAt(world, pos)) {
            EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
            if (fakePlayer == null) {
                return null;
            }
            BlockSnapshot snap = new BlockSnapshot(world, pos, newState);
            fakePlayer.setHeldItem(EnumHand.MAIN_HAND, toPlace);
            BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(snap, null, fakePlayer, EnumHand.MAIN_HAND);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && world.setBlockState(pos, newState)) {
                fakePlayer.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                newState.getBlock().onBlockPlacedBy(world, pos, newState, fakePlayer, toPlace);
                return newState;
            }
        }

        return null;
    }

    /**
     * Try to break the block at the given position. If the block has any drops, but no drops pass the filter, then the
     * block will not be broken. Liquid, air & unbreakable blocks (bedrock etc.) will never be broken.  Drops will be
     * available via the DropResult object, organised by whether or not they passed the filter.
     *
     * @param world     the world
     * @param pos       the block position
     * @param filter    filter for the block's drops
     * @param silkTouch use silk touch when breaking the block
     * @param fortune   use fortune when breaking the block
     * @return a drop result object
     */
    public static BreakResult tryBreakBlock(World world, BlockPos pos, Filter filter, boolean silkTouch, int fortune) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0 || block instanceof BlockLiquid) {
            return BreakResult.NOT_BROKEN;
        }

        EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
        List<ItemStack> allDrops = getDrops(world, pos, fakePlayer, silkTouch, fortune);

        Map<Boolean, List<ItemStack>> groups = allDrops.stream().collect(Collectors.partitioningBy(filter));
        if (allDrops.isEmpty() || !groups.get(true).isEmpty()) {
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                world.setBlockToAir(pos);
                return new BreakResult(true, groups);
            }
        }
        return BreakResult.NOT_BROKEN;
    }

    private static List<ItemStack> getDrops(World world, BlockPos pos, EntityPlayer player, boolean silkTouch, int fortune) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (silkTouch) {
            Item item = Item.getItemFromBlock(block);
            if (item == Items.AIR) {
                return Collections.emptyList();
            } else {
                return Lists.newArrayList(new ItemStack(item, 1, block.getMetaFromState(state)));
            }
        }

        List<ItemStack> drops = block.getDrops(world, pos, state, fortune);
        float dropChance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, 1.0F, false, player);

        return drops.stream().filter(s -> world.rand.nextFloat() <= dropChance).collect(Collectors.toList());
    }

    public static String getBlockName(World w, BlockPos pos) {
        if (w == null) {
            return null;
        }
        IBlockState state = w.getBlockState(pos);
        if (state.getBlock().isAir(state, w, pos)) {
            return "";
        } else {
            ItemStack stack = state.getBlock().getItem(w, pos, state);
            if (!stack.isEmpty()) {
                return stack.getDisplayName();
            } else {
                return state.getBlock().getLocalizedName();
            }
        }
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
