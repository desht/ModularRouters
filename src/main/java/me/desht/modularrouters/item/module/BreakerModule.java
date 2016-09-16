package me.desht.modularrouters.item.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.logic.CompiledBreakerModule;
import me.desht.modularrouters.logic.CompiledModule;
import me.desht.modularrouters.util.FakePlayer;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BreakerModule extends Module {
    @Override
    public void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addUsageInformation(itemstack, player, list, par4);
        Map<Enchantment, Integer> ench = EnchantmentHelper.getEnchantments(itemstack);
        if (ench.isEmpty()) {
            MiscUtil.appendMultiline(list, "itemText.misc.enchantBreakerHint");
        }
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemModule.makeItemStack(ItemModule.ModuleType.BREAKER),
                ModItems.blankModule, Items.IRON_PICKAXE);
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter tileEntityItemRouter, ItemStack stack) {
        return new CompiledBreakerModule(tileEntityItemRouter, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModule compiled) {
        if (compiled.getDirection() != Module.RelativeDirection.NONE && !router.isBufferFull()) {
            World world = router.getWorld();
            if (!(world instanceof  WorldServer)) {
                return false;
            }
            BlockPos pos = compiled.getTarget().pos;
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (block.isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0 || block instanceof BlockLiquid) {
                return false;
            }
            Item item = Item.getItemFromBlock(block);
            if (item == null) {
                return false;
            }
            if (compiled.getFilter().pass(new ItemStack(item, 1, block.getMetaFromState(state)))) {
                EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
                BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, fakePlayer);
                MinecraftForge.EVENT_BUS.post(breakEvent);
                if (!breakEvent.isCanceled()) {
                    List<ItemStack> drops = getDrops(world, pos, fakePlayer, getFortuneLevel(compiled), canSilkTouch(compiled));
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

    private List<ItemStack> getDrops(World world, BlockPos pos, EntityPlayer player, int fortuneLevel, boolean silkTouch) {
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

        List<ItemStack> drops = block.getDrops(world, pos, state, fortuneLevel);
        float dropChance = ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortuneLevel, 1.0F, false, player);

        return drops.stream().filter(s -> world.rand.nextFloat() <= dropChance).collect(Collectors.toList());
    }

    private int getFortuneLevel(CompiledModule settings) {
        return settings instanceof CompiledBreakerModule ?
                ((CompiledBreakerModule) settings).getFortune() : 0;
    }

    private boolean canSilkTouch(CompiledModule settings) {
        return settings instanceof CompiledBreakerModule && ((CompiledBreakerModule) settings).isSilkTouch();
    }
}
