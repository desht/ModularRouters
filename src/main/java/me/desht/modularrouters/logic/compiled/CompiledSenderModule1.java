package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.SenderModule1;
import me.desht.modularrouters.network.ParticleBeamMessage;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
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

public class CompiledSenderModule1 extends CompiledModule {
    public CompiledSenderModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        ItemStackHandler buffer = (ItemStackHandler) router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);
        if (bufferStack != null && getFilter().pass(bufferStack)) {
            PositionedItemHandler target = findTargetInventory(router);
            if (target != null) {
                int nToSend = router.getItemsPerTick();
                if (getRegulationAmount() > 0) {
                    int existing = InventoryUtils.countItems(bufferStack, target.handler, getRegulationAmount(), !getFilter().getFlags().isIgnoreMeta());
                    nToSend = Math.min(nToSend, getRegulationAmount() - existing);
                    if (nToSend <= 0) {
                        return false;
                    }
                }
                int sent = InventoryUtils.transferItems(buffer, target.handler, 0, nToSend);
                if (sent > 0) {
                    if (Config.senderParticles) {
                        playParticles(router, target.pos, (float)sent / (float)bufferStack.getMaxStackSize());
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    protected void playParticles(TileEntityItemRouter router, BlockPos targetPos, float val) {
        Vec3d vec1 = new Vec3d(router.getPos()).addVector(0.5, 0.5, 0.5);
        Vec3d vec2 = new Vec3d(targetPos).addVector(0.5, 0.5, 0.5);
        Color color = Color.getHSBColor(val, 1.0f, 1.0f);
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(router.getWorld().provider.getDimension(), vec1.xCoord, vec1.yCoord, vec1.zCoord, 32);
        ModularRouters.network.sendToAllAround(new ParticleBeamMessage(vec1.xCoord, vec1.yCoord, vec1.zCoord, vec2.xCoord, vec2.yCoord, vec2.zCoord, color), point);
    }

    protected PositionedItemHandler findTargetInventory(TileEntityItemRouter router) {
        if (getDirection() == Module.RelativeDirection.NONE) {
            return null;
        }
        BlockPos pos = getTarget().pos;
        EnumFacing face = getTarget().face;
        for (int i = 1; i < SenderModule1.maxDistance(router); i++) {
            World world = router.getWorld();
            IItemHandler handler = InventoryUtils.getInventory(world, pos, face);
            if (handler != null) {
                return new PositionedItemHandler(pos, handler);
            } else if (!isPassable(world, pos, face)) {
                return null;
            }
            pos = pos.offset(getFacing());
        }
        return null;
    }

    private boolean isPassable(World w, BlockPos pos, EnumFacing face) {
        IBlockState state = w.getBlockState(pos);
        return !state.getBlock().isBlockSolid(w, pos, face) || !state.isOpaqueCube();
    }

    public static class PositionedItemHandler {
        private final BlockPos pos;
        private final IItemHandler handler;

        public PositionedItemHandler(BlockPos pos, IItemHandler handler) {
            this.pos = pos;
            this.handler = handler;
        }
    }
}
