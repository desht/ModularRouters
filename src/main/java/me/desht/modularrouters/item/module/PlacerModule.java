package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.FakePlayer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class PlacerModule extends Module {
    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack buffer = router.getBufferItemStack();
        if (buffer == null || settings.getDirection() == RelativeDirection.NONE || !settings.getFilter().pass(buffer)) {
            return false;
        }

        BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
        World world = router.getWorld();
        IBlockState currentState = world.getBlockState(pos);
        if (!currentState.getBlock().isAir(currentState, world, pos) || !currentState.getBlock().isReplaceable(world, pos)) {
            return false;
        }

        ItemStack toPlace = router.getBuffer().extractItem(0, 1, true);
        IBlockState newState = getPlaceableState(toPlace);
        if (newState == null) {
            return false;
        }

        if (newState.getBlock().canPlaceBlockAt(world, pos)) {
            EntityPlayer fakePlayer = FakePlayer.getFakePlayer((WorldServer) world, pos).get();
            BlockSnapshot snap = new BlockSnapshot(world, pos, newState);
            BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(snap, null, fakePlayer);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled() && world.setBlockState(pos, newState)) {
                router.getBuffer().extractItem(0, 1, false);
                if (Config.placerParticles) {
                    world.playEvent(2001, pos, Block.getStateId(newState));
                }
                return true;
            }
        }

        return false;
    }

    public static final String[] REED_ITEM = new String[] { "block", "field_150935_a", "a" };

    private IBlockState getPlaceableState(ItemStack stack) {
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
}
