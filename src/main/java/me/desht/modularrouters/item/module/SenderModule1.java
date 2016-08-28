package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.network.ParticleBeamMessage;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.awt.*;

public class SenderModule1 extends Module {
    public static class SenderTarget {
        private final BlockPos pos;
        private final IItemHandler handler;

        public SenderTarget(BlockPos pos, IItemHandler handler) {
            this.pos = pos;
            this.handler = handler;
        }
    }

    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);
        if (bufferStack != null && settings.getFilter().pass(bufferStack)) {
            SenderTarget target = findTargetInventory(router, settings);
            if (target != null) {
                int sent = InventoryUtils.transferItems(buffer, target.handler, 0, router.getItemsPerTick());
                if (sent > 0) {
                    if (Config.senderParticles) {
                        playParticles(router, settings, target.pos, (float)sent / (float)bufferStack.getMaxStackSize());
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected Object[] getExtraUsageParams() {
        return new Object[] { Config.Defaults.SENDER1_BASE_RANGE, Config.Defaults.SENDER1_MAX_RANGE };
    }

    protected void playParticles(TileEntityItemRouter router, CompiledModuleSettings settings, BlockPos targetPos, float val) {
        Vec3d vec1 = new Vec3d(router.getPos()).addVector(0.5, 0.5, 0.5);
        Vec3d vec2 = new Vec3d(targetPos).addVector(0.5, 0.5, 0.5);
        Color color = Color.getHSBColor(val, 1.0f, 1.0f);
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(router.getWorld().provider.getDimension(), vec1.xCoord, vec1.yCoord, vec1.zCoord, 32);
        ModularRouters.network.sendToAllAround(new ParticleBeamMessage(vec1.xCoord, vec1.yCoord, vec1.zCoord, vec2.xCoord, vec2.yCoord, vec2.zCoord, color), point);
    }

    protected SenderTarget findTargetInventory(TileEntityItemRouter router, CompiledModuleSettings settings) {
        if (settings.getDirection() == Module.RelativeDirection.NONE) {
            return null;
        }
        BlockPos pos = settings.getTarget().pos;
        EnumFacing face = settings.getTarget().face;
        EnumFacing faceOpposite = face.getOpposite();
        for (int i = 1; i < SenderModule1.maxDistance(router); i++) {
            World world = router.getWorld();
            IItemHandler handler = InventoryUtils.getInventory(world, pos, face);
            if (handler != null) {
                return new SenderTarget(pos, handler);
            } else if (!isPassable(world, pos, face)) {
                return null;
            }
            pos = pos.offset(faceOpposite);
        }
        return null;
    }

    public static int maxDistance(TileEntityItemRouter router) {
        return Config.sender1BaseRange + Math.min(router.getRangeUpgrades(), Config.sender1BaseRange);
    }

    private boolean isPassable(World w, BlockPos pos, EnumFacing face) {
        IBlockState state = w.getBlockState(pos);
        if (!state.getBlock().isBlockSolid(w, pos, face)) {
            return true;
        }
        if (!state.isOpaqueCube()) {
            return true;
        }
        return false;
    }
}
