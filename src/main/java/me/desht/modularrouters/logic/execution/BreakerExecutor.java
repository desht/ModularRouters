package me.desht.modularrouters.logic.execution;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.logic.CompiledBreakerModuleSettings;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;
import java.util.stream.Collectors;

public class BreakerExecutor extends ModuleExecutor {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack bufferStack = router.getBufferItemStack();
        if (settings.getDirection() != AbstractModule.RelativeDirection.NONE
                && (bufferStack == null || bufferStack.stackSize < bufferStack.getMaxStackSize())) {
            World world = router.getWorld();
            BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            Item item = Item.getItemFromBlock(block);
            if (world instanceof WorldServer
                    && !block.isAir(state, world, pos) && !(block instanceof BlockLiquid)
                    /* && item != null */ && settings.getFilter().pass(new ItemStack(item, 1, block.getMetaFromState(state)))) {
                float hardness = state.getBlockHardness(world, pos);
                if (hardness >= 0.0f) {
                    EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
                    BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
                    MinecraftForge.EVENT_BUS.post(breakEvent);
                    if (!breakEvent.isCanceled()) {
                        List<ItemStack> drops = getDrops(world, pos, fakePlayer, getFortuneLevel(settings), canSilkTouch(settings));
                        for (ItemStack drop : drops) {
                            ItemStack excess = router.getBuffer().insertItem(0, drop, false);
                            if (excess != null) {
                                dropItems(world, pos, excess);
                            }
//                            if (bufferStack == null) {
//                                router.setBufferItemStack(drop);
//                                bufferStack = router.getBufferItemStack();
//                            } else if (ItemHandlerHelper.canItemStacksStack(drop, bufferStack)) {
//                                int available = bufferStack.getMaxStackSize() - bufferStack.stackSize;
//                                bufferStack.stackSize += drop.splitStack(available).stackSize;
//                                if (drop.stackSize > 0) {
//                                    dropItems(world, pos, drop);
//                                }
//                            } else {
//                                // drop can't go in the router
//                                dropItems(world, pos, drop);
//                            }
                        }
                        if (Config.breakerParticles) {
                            world.playEvent(2001, pos, Block.getStateId(state));
                        }
                        world.setBlockToAir(pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void dropItems(World world, BlockPos pos, ItemStack stack) {
        EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        world.spawnEntityInWorld(item);
    }

    private List<ItemStack> getDrops(World world, BlockPos pos, EntityPlayer player, int fortuneLevel, boolean silkTouch) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (silkTouch) {
            ItemStack drop = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, state.getBlock().getMetaFromState(state));
            return Lists.newArrayList(drop);
        }

        List<ItemStack> drops = block.getDrops(world, pos, state, fortuneLevel);
        float dropChance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortuneLevel, 1.0F, false, player);

        List<ItemStack> result = drops.stream().filter(s -> world.rand.nextFloat() <= dropChance).collect(Collectors.toList());

        return result;
    }

    private int getFortuneLevel(CompiledModuleSettings settings) {
        return settings instanceof CompiledBreakerModuleSettings ?
                ((CompiledBreakerModuleSettings) settings).getFortune() : 0;
    }

    private boolean canSilkTouch(CompiledModuleSettings settings) {
        return settings instanceof CompiledBreakerModuleSettings && ((CompiledBreakerModuleSettings) settings).isSilkTouch();
    }
}
