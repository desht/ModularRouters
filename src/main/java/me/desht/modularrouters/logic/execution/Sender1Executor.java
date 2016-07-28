package me.desht.modularrouters.logic.execution;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.AbstractModule;
import me.desht.modularrouters.item.module.ItemSenderModule1;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class Sender1Executor extends ModuleExecutor {
    class SenderTarget {
        private final BlockPos pos;
        private final IItemHandler handler;

        SenderTarget(BlockPos pos, IItemHandler handler) {
            this.pos = pos;
            this.handler = handler;
        }
    }

    private double particlePos = 0.0;

    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);
        if (bufferStack != null && settings.getFilter().pass(bufferStack)) {
            SenderTarget target = findTargetInventory(router, settings);
            int sent = InventoryUtils.transferItems(buffer, target.handler, 0, router.getItemsPerTick());
            if (sent > 0) {
                if (Config.senderParticles) {
                    playParticles(router, settings, target.pos);
                }
                return true;
            } else {
                return false;
            }
        }

        resetParticlePos();
        return false;
    }

    protected void resetParticlePos() {
        particlePos = 0.0;
    }

    protected void playParticles(TileEntityItemRouter router, CompiledModuleSettings settings, BlockPos targetPos) {
//        BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
        Vec3d vec1 = new Vec3d(router.getPos());
        Vec3d vec2 = new Vec3d(targetPos);
        Vec3d vec3 = vec2.subtract(vec1).scale(particlePos).add(vec1).addVector(0.5, 0.5, 0.5);
        ((WorldServer) router.getWorld()).spawnParticle(EnumParticleTypes.REDSTONE, false, vec3.xCoord, vec3.yCoord, vec3.zCoord, 2, 0.05, 0.05, 0.05, 0.001);
        particlePos += 0.1;
        if (particlePos > 1.0) {
            resetParticlePos();
        }
    }

    protected SenderTarget findTargetInventory(TileEntityItemRouter router, CompiledModuleSettings settings) {
        if (settings.getDirection() == AbstractModule.RelativeDirection.NONE) {
            return null;
        }
        BlockPos pos = router.getRelativeBlockPos(settings.getDirection());
        EnumFacing facing = router.getAbsoluteFacing(settings.getDirection());
        EnumFacing facingOpposite = facing.getOpposite();
        for (int i = 1; i < ItemSenderModule1.maxDistance(router); i++) {
            World world = router.getWorld();
            IItemHandler handler = InventoryUtils.getInventory(world, pos, facingOpposite);
            if (handler != null) {
                return new SenderTarget(pos, handler);
            } else if (world.getBlockState(pos).getBlock().isBlockSolid(world, pos, facingOpposite)) {
                return null;
            }
            pos = pos.offset(facing);
        }
        return null;
    }
}
