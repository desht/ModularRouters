package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ItemBeamMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class CompiledSenderModule1 extends CompiledModule {
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
                        playParticles(router, target.pos, ItemHandlerHelper.copyStackWithSize(bufferStack, sent));
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    void playParticles(TileEntityItemRouter router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE) < 2) {
            Vec3d vec1 = new Vec3d(router.getPos());
            PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(vec1.x, vec1.y, vec1.z, 32, router.getWorld().dimension.getType());
            PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                    new ItemBeamMessage(router.getPos(), targetPos, stack, getBeamColor(), router.getTickRate()));
        }
    }

    protected int getBeamColor() {
        return 0xFFC000;
    }

    PositionedItemHandler findTargetInventory(TileEntityItemRouter router) {
        ModuleTarget target = getEffectiveTarget(router);
        if (target != null) {
            IItemHandler handler = target.getItemHandler();
            return handler == null ? null : new PositionedItemHandler(target.gPos.getPos(), handler);
        }
        return null;
    }

    @Override
    public ModuleTarget getEffectiveTarget(TileEntityItemRouter router) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(getTarget().gPos.getPos());
        Direction face = getTarget().face;
        World world = router.getWorld();
        for (int i = 1; i <= getRange(); i++) {
            if (world.getTileEntity(pos) != null) {
                GlobalPos gPos = GlobalPos.of(world.getDimension().getType(), pos.toImmutable());
                return new ModuleTarget(gPos, face, BlockUtil.getBlockName(world, pos));
            } else if (!isPassable(world, pos, face)) {
                return null;
            }
            pos.move(getFacing());
        }
        return null;
    }

    private boolean isPassable(World w, BlockPos pos, Direction face) {
        BlockState state = w.getBlockState(pos);
        return !Block.hasSolidSide(state, w, pos, face.getOpposite()) || !state.isOpaqueCube(w, pos);
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
