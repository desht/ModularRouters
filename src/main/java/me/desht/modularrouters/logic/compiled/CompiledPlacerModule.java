package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.util.BlockUtil;
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

public class CompiledPlacerModule extends CompiledModule {
    public CompiledPlacerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (getDirection() == Module.RelativeDirection.NONE) {
            return false;
        }
        ItemStack toPlace = router.peekBuffer(1);
        if (toPlace == null || !getFilter().pass(toPlace)) {
            return false;
        }
        World w = router.getWorld();
        BlockPos pos = getTarget().pos;
        if (BlockUtil.tryPlaceAsBlock(toPlace, w, pos)) {
            if (Config.placerParticles) {
                w.playEvent(2001, pos, Block.getStateId(w.getBlockState(pos)));
            }
            router.extractBuffer(1);
            return true;
        } else {
            return false;
        }
    }
}
