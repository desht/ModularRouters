package me.desht.modularrouters.util;

import com.google.common.collect.Lists;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlockUtil {
    private static final String[] REED_ITEM = new String[] { "block", "field_150935_a", "a" };

    public static IBlockState getPlaceableState(ItemStack stack) {
        // With thanks to Vazkii for inspiration from the Rannuncarpus code :)
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).block.getStateFromMeta(item.getMetadata(stack.getItemDamage()));
        } else if (item instanceof ItemBlockSpecial) {
            return ((Block) ReflectionHelper.getPrivateValue(ItemBlockSpecial.class, (ItemBlockSpecial) item, REED_ITEM)).getDefaultState();
        } else if (item instanceof ItemRedstone){
            return Blocks.REDSTONE_WIRE.getDefaultState();
        } else {
            return null;
        }
    }

    /**
     * Try to place the given item as a block in the world.
     *
     * @param toPlace item to place
     * @param world the world
     * @param pos position in the world to place at
     * @return the new block state if successful, null otherwise
     */
    public static IBlockState tryPlaceAsBlock(ItemStack toPlace, World world, BlockPos pos) {
        IBlockState currentState = world.getBlockState(pos);
        if (!currentState.getBlock().isAir(currentState, world, pos) || !currentState.getBlock().isReplaceable(world, pos)) {
            return null;
        }

        IBlockState newState = getPlaceableState(toPlace);
        if (newState != null && newState.getBlock().canPlaceBlockAt(world, pos)) {
            EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
            if (fakePlayer == null) {
                return null;
            }
            BlockSnapshot snap = new BlockSnapshot(world, pos, newState);
            BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(snap, null, fakePlayer);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && world.setBlockState(pos, newState)) {
                return newState;
            }
        }

        return null;
    }

    public static boolean tryBreakBlock(World world, BlockPos pos, Filter filter, List<ItemStack> drops, boolean silkTouch, int fortune) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0 || block instanceof BlockLiquid) {
            return false;
        }
        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return false;
        }
        if (filter.pass(new ItemStack(item, 1, block.getMetaFromState(state)))) {
            EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                drops.addAll(getDrops(world, pos, fakePlayer, silkTouch, fortune));
                world.setBlockToAir(pos);
                return true;
            }
        }
        return false;
    }

    private static List<ItemStack> getDrops(World world, BlockPos pos, EntityPlayer player, boolean silkTouch, int fortune) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (silkTouch) {
            Item item = Item.getItemFromBlock(block);
            if (item == null) {
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
            if (stack != null) {
                return stack.getDisplayName();
            } else {
                return state.getBlock().getLocalizedName();
            }
        }
    }
}
