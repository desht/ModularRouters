package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.network.ParticleBeamMessage;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.awt.*;

public class CompiledSenderModule1 extends CompiledModule {
    private static final Color particleColor = Color.ORANGE;

    public CompiledSenderModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        IItemHandler buffer = router.getBuffer();
        ItemStack bufferStack = buffer.getStackInSlot(0);
        if (getFilter().test(bufferStack)) {
            PositionedItemHandler target = findTargetInventory(router);
            if (target != null) {
                int nToSend = getItemsPerTick(router);
                if (getRegulationAmount() > 0) {
                    int existing = InventoryUtils.countItems(bufferStack, target.handler, getRegulationAmount(), !getFilter().getFlags().isIgnoreDamage());
                    nToSend = Math.min(nToSend, getRegulationAmount() - existing);
                    if (nToSend <= 0) {
                        return false;
                    }
                }
                int sent = InventoryUtils.transferItems(buffer, target.handler, 0, nToSend);
                if (sent > 0) {
                    if (ConfigHandler.MODULE.senderParticles.get()) {
                        playParticles(router, target.pos);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    void playParticles(TileEntityItemRouter router, BlockPos targetPos) {
        if (router.getUpgradeCount(ObjectRegistry.MUFFLER_UPGRADE) < 2) {
            Vec3d vec1 = new Vec3d(router.getPos()).add(0.5, 0.5, 0.5);
            Vec3d vec2 = new Vec3d(targetPos).add(0.5, 0.5, 0.5);
            PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(vec1.x, vec1.y, vec1.z, 32, router.getWorld().dimension.getType());
            PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                    new ParticleBeamMessage(vec1.x, vec1.y, vec1.z, vec2.x, vec2.y, vec2.z, particleColor, 0.3f));
        }
    }

    PositionedItemHandler findTargetInventory(TileEntityItemRouter router) {
        ModuleTarget target = getActualTarget(router);
        if (target != null) {
            IItemHandler handler = target.getItemHandler();
            return handler == null ? null : new PositionedItemHandler(target.pos, handler);
        }
        return null;
    }

    @Override
    public ModuleTarget getActualTarget(TileEntityItemRouter router) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(getTarget().pos);
        EnumFacing face = getTarget().face;
        World world = router.getWorld();
        for (int i = 1; i <= getRange(); i++) {
            if (world.getTileEntity(pos) != null) {
                return new ModuleTarget(MiscUtil.getDimensionForWorld(world), pos.toImmutable(), face, BlockUtil.getBlockName(world, pos));
            } else if (!isPassable(world, pos, face)) {
                return null;
            }
            pos.move(getFacing());
        }
        return null;
    }

    private boolean isPassable(World w, BlockPos pos, EnumFacing face) {
        IBlockState state = w.getBlockState(pos);
        return state.getBlockFaceShape(w, pos, face) != BlockFaceShape.SOLID || !state.isOpaqueCube(w, pos);
    }

    static class PositionedItemHandler {
        private final BlockPos pos;
        private final IItemHandler handler;

        PositionedItemHandler(BlockPos pos, IItemHandler handler) {
            this.pos = pos;
            this.handler = handler;
        }
    }
}
