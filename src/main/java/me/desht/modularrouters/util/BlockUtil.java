package me.desht.modularrouters.util;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.filter.Filter;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockUtil {
    private static final String[] REED_ITEM = new String[]{"block", "field_150935_a", "a"};

    private static IBlockState getPlaceableState(EntityPlayer fakePlayer, ItemStack stack, World world, BlockPos pos, EnumFacing facing, EnumFacing horizFacing) {
        // With thanks to Vazkii for inspiration from the Rannuncarpus code, although it's changed a lot since...
        try {
            Item item = stack.getItem();
            IBlockState res = null;
            if (item instanceof ItemBlock) {
                float hitX = (float) (fakePlayer.posX - pos.getX());
                float hitY = (float) (fakePlayer.posY - pos.getY());
                float hitZ = (float) (fakePlayer.posZ - pos.getZ());
                int meta = item.getMetadata(stack.getItemDamage());
                res = ((ItemBlock) item).getBlock().getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, fakePlayer, EnumHand.MAIN_HAND);
            } else if (item instanceof ItemBlockSpecial) {
                res = ((Block) ReflectionHelper.getPrivateValue(ItemBlockSpecial.class, (ItemBlockSpecial) item, REED_ITEM)).getDefaultState();
            } else if (item instanceof ItemRedstone) {
                res = Blocks.REDSTONE_WIRE.getDefaultState();
            } else if (item instanceof ItemDye && EnumDyeColor.byDyeDamage(stack.getMetadata()) == EnumDyeColor.BROWN) {
                res = getCocoaBeanState(fakePlayer, world, pos);
                if (res != null) {
                    facing = res.getValue(BlockHorizontal.FACING);
                }
            } else if (item instanceof IPlantable) {
                IBlockState state = ((IPlantable) item).getPlant(world, pos);
                res = ((state.getBlock() instanceof BlockCrops) && ((BlockCrops) state.getBlock()).canBlockStay(world, pos, state)) ? state : null;
            } else if (item instanceof ItemSkull) {
                res = Blocks.SKULL.getDefaultState();
                // try to place skull on horizontal surface below if possible
                BlockPos pos2 = pos.down();
                if (world.getBlockState(pos2).isSideSolid(world, pos2, EnumFacing.UP)) {
                    facing = EnumFacing.UP;
                }
            }
            if (res != null) {
                if (res.getProperties().containsKey(BlockDirectional.FACING)) {
                    res = res.withProperty(BlockDirectional.FACING, facing);
                } else if (res.getProperties().containsKey(BlockHorizontal.FACING)) {
                    res = res.withProperty(BlockHorizontal.FACING, facing.getHorizontalIndex() >= 0 ? facing : horizFacing);
                }
            }
            return res;
        } catch (IllegalArgumentException e) {
            // See https://github.com/desht/ModularRouters/issues/25
            // Thanks Actually Additions for generating unplaceable random blocks, like double slabs :)
            return null;
        }
    }

    private static IBlockState getCocoaBeanState(EntityPlayer fakePlayer, World world, BlockPos pos) {
        // try to find a jungle log in any horizontal direction
        for (EnumFacing f : EnumFacing.HORIZONTALS) {
            IBlockState state = world.getBlockState(pos.offset(f));
            if (state.getBlock() == Blocks.LOG && state.getValue(BlockOldLog.VARIANT) == BlockPlanks.EnumType.JUNGLE) {
                float hitX = (float) (fakePlayer.posX - pos.getX());
                float hitY = (float) (fakePlayer.posY - pos.getY());
                float hitZ = (float) (fakePlayer.posZ - pos.getZ());
                fakePlayer.rotationYaw = getYawFromFacing(f);  // fake player must face the jungle log
                return Blocks.COCOA.getStateForPlacement(world, pos, f.getOpposite(), hitX, hitY, hitZ, 0, fakePlayer, EnumHand.MAIN_HAND);
            }
        }
        return null;
    }

    private static float getYawFromFacing(EnumFacing facing) {
        switch (facing) {
            case WEST: return 90f;
            case NORTH: return 180f;
            case EAST: return 270f;
            case SOUTH: return 0f;
            default: return 0f; // shouldn't happen
        }
    }

    private static void handleSkullPlacement(World worldIn, BlockPos pos, ItemStack stack, EnumFacing facing) {
        // adapted from ItemSkull#onItemUse()

        int i = 0;
        if (worldIn.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.UP) {
            i = MathHelper.floor((double) (facing.getHorizontalAngle() * 16.0F / 360.0F) + 0.5D) & 15;
        }

        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntitySkull) {
            TileEntitySkull tileentityskull = (TileEntitySkull) tileentity;
            if (stack.getMetadata() == 3) {   // player head
                GameProfile gameprofile = null;
                if (stack.hasTagCompound()) {
                    NBTTagCompound nbttagcompound = stack.getTagCompound();
                    if (nbttagcompound.hasKey("SkullOwner", Constants.NBT.TAG_COMPOUND)) {
                        gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                    } else if (nbttagcompound.hasKey("SkullOwner", Constants.NBT.TAG_STRING) && !nbttagcompound.getString("SkullOwner").isEmpty()) {
                        gameprofile = new GameProfile(null, nbttagcompound.getString("SkullOwner"));
                    }
                }
                tileentityskull.setPlayerProfile(gameprofile);
            } else {
                tileentityskull.setType(stack.getMetadata());
            }

            tileentityskull.setSkullRotation(i);  // skull will face the router fake-player
            Blocks.SKULL.checkWitherSpawn(worldIn, pos, tileentityskull);
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
    public static IBlockState tryPlaceAsBlock(ItemStack toPlace, World world, BlockPos pos, EnumFacing facing, EnumFacing horizFacing) {
        IBlockState currentState = world.getBlockState(pos);
        if (!currentState.getBlock().isReplaceable(world, pos)) {
            return null;
        }

        FakePlayer fakePlayer = FakePlayerManager.getFakePlayer((WorldServer) world, pos);
        if (fakePlayer == null) {
            return null;
        }
        fakePlayer.rotationYaw = getYawFromFacing(facing);

        IBlockState newState = getPlaceableState(fakePlayer, toPlace, world, pos, facing, horizFacing);
        if (newState != null && newState.getBlock().canPlaceBlockAt(world, pos)) {
            BlockSnapshot snap = new BlockSnapshot(world, pos, newState);
            fakePlayer.setHeldItem(EnumHand.MAIN_HAND, toPlace);
            BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(snap, Blocks.AIR.getDefaultState(), fakePlayer, EnumHand.MAIN_HAND);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && world.setBlockState(pos, newState, 3)) {
                fakePlayer.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                ItemBlock.setTileEntityNBT(world, fakePlayer, pos, toPlace);
                newState.getBlock().onBlockPlacedBy(world, pos, newState, fakePlayer, toPlace);
                if (newState.getBlock() == Blocks.SKULL) {
                    handleSkullPlacement(world, pos, toPlace, facing);
                }
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
     * @param pick      pickaxe being used
     * @return a drop result object
     */
    public static BreakResult tryBreakBlock(World world, BlockPos pos, Filter filter, ItemStack pick) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0 || block instanceof BlockLiquid) {
            return BreakResult.NOT_BROKEN;
        }

        FakePlayer fakePlayer = FakePlayerManager.getFakePlayer((WorldServer) world, pos);
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, pick);
        if (ConfigHandler.module.breakerHarvestLimit && !ForgeHooks.canHarvestBlock(block, fakePlayer, world, pos)) {
            return BreakResult.NOT_BROKEN;
        }
        List<ItemStack> allDrops = getDrops(world, pos, fakePlayer, pick);

        Map<Boolean, List<ItemStack>> groups = allDrops.stream().collect(Collectors.partitioningBy(filter));
        if (allDrops.isEmpty() || !groups.get(true).isEmpty()) {
            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
            MinecraftForge.EVENT_BUS.post(breakEvent);
            if (!breakEvent.isCanceled()) {
                if (block instanceof BlockShulkerBox) {
                    ItemStack stack = specialShulkerBoxHandling(world, pos);
                    groups = new HashMap<>();
                    groups.put(true, Lists.newArrayList(stack));
                }
                world.setBlockToAir(pos);
                return new BreakResult(true, groups);
            }
        }
        return BreakResult.NOT_BROKEN;
    }

    /**
     * Work around extra logic in BlockShulkerBox#breakBlock
     * Shulker box breakBlock() method appears to be unique among all Minecraft classes
     * in that will drop an item directly.  Sigh.
     */
    private static ItemStack specialShulkerBoxHandling(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityShulkerBox) {
            TileEntityShulkerBox tesb = (TileEntityShulkerBox) te;
            if (!tesb.isCleared() && tesb.shouldDrop()) {
                ItemStack itemstack = new ItemStack(Item.getItemFromBlock(world.getBlockState(pos).getBlock()));
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound.setTag("BlockEntityTag", tesb.saveToNbt(nbttagcompound1));
                itemstack.setTagCompound(nbttagcompound);
                if (tesb.hasCustomName()) {
                    itemstack.setStackDisplayName(tesb.getName());
                    tesb.setCustomName("");
                }
                tesb.clear();  // stops BlockShulkerBox#breakBlock dropping it as an item
                return itemstack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<ItemStack> getDrops(World world, BlockPos pos, EntityPlayer player, ItemStack pick) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        boolean silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, pick) > 0;
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, pick);
        if (silkTouch) {
            Item item = Item.getItemFromBlock(block);
            if (item == Items.AIR) {
                return Collections.emptyList();
            } else {
                return Lists.newArrayList(new ItemStack(item, 1, block.getMetaFromState(state)));
            }
        } else {
            NonNullList<ItemStack> drops = NonNullList.create();
            block.getDrops(drops, world, pos, state, fortune);
            float dropChance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, 1.0F, false, player);
            return drops.stream().filter(s -> world.rand.nextFloat() <= dropChance).collect(Collectors.toList());
        }
    }

    public static String getBlockName(World w, BlockPos pos) {
        if (w == null) {
            return null;
        }
        IBlockState state = w.getBlockState(pos);
        if (state.getBlock().isAir(state, w, pos)) {
            return "";
        } else {
            ItemStack stack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().damageDropped(state));
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
