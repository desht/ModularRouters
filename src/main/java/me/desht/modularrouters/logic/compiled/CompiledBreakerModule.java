package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CompiledBreakerModule extends CompiledModule {
    private final boolean silkTouch;
    private final int fortune;

    public CompiledBreakerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
        fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (getDirection() != Module.RelativeDirection.NONE && !router.isBufferFull()) {
            World world = router.getWorld();
            if (!(world instanceof WorldServer)) {
                return false;
            }
            BlockPos pos = getTarget().pos;
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block.isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0 || block instanceof BlockLiquid) {
                return false;
            }
            Item item = Item.getItemFromBlock(block);
            if (item == null) {
                return false;
            }
            if (getFilter().pass(new ItemStack(item, 1, block.getMetaFromState(state)))) {
                EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (!breakEvent.isCanceled()) {
                    List<ItemStack> drops = getDrops(world, pos, fakePlayer);
                    for (ItemStack drop : drops) {
                        ItemStack excess = router.getBuffer().insertItem(0, drop, false);
                        if (excess != null) {
                            dropItems(world, pos, excess);
                        }
                    }
                    if (Config.breakerParticles) {
                        world.playEvent(2001, pos, Block.getStateId(state));
                    }
                    world.setBlockToAir(pos);
                    return true;
                }
            }
        }
        return false;
    }

    private void dropItems(World world, BlockPos pos, ItemStack stack) {
        EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        world.spawnEntityInWorld(item);
    }

    private List<ItemStack> getDrops(World world, BlockPos pos, EntityPlayer player) {
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
}
