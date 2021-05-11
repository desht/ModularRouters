package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ItemBeamMessage;
import me.desht.modularrouters.network.PacketHandler;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
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
            PositionedItemHandler positionedItemHandler = findTargetInventory(router);
            if (positionedItemHandler.isValid()) {
                int nToSend = getItemsPerTick(router);
                if (getRegulationAmount() > 0) {
                    int existing = InventoryUtils.countItems(bufferStack, positionedItemHandler.handler, getRegulationAmount(), !getFilter().getFlags().isIgnoreDamage());
                    nToSend = Math.min(nToSend, getRegulationAmount() - existing);
                    if (nToSend <= 0) {
                        return false;
                    }
                }
                int sent = InventoryUtils.transferItems(buffer, positionedItemHandler.handler, 0, nToSend);
                if (sent > 0) {
                    if (MRConfig.Common.Module.senderParticles) {
                        playParticles(router, positionedItemHandler.pos, ItemHandlerHelper.copyStackWithSize(bufferStack, sent));
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
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
//            Vector3d vec1 = Vector3d.atCenterOf(router.getBlockPos());
//            PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(vec1.x, vec1.y, vec1.z, 32, router.getLevel().dimension());
            PacketHandler.NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> router.getLevel().getChunkAt(router.getBlockPos())),
                    new ItemBeamMessage(router, targetPos, false, stack, getBeamColor(), router.getTickRate(), false));
        }
    }

    protected int getBeamColor() {
        return 0xFFC000;
    }

    PositionedItemHandler findTargetInventory(TileEntityItemRouter router) {
        ModuleTarget target = getEffectiveTarget(router);
        if (target != null) {
            return target.getItemHandler().map(h -> new PositionedItemHandler(target.gPos.pos(), h)).orElse(PositionedItemHandler.INVALID);
        }
        return PositionedItemHandler.INVALID;
    }

    @Override
    public ModuleTarget getEffectiveTarget(TileEntityItemRouter router) {
        BlockPos p0 = getTarget().gPos.pos();
        BlockPos.Mutable pos = new BlockPos.Mutable(p0.getX(), p0.getY(), p0.getZ());
        Direction face = getTarget().face;
        World world = router.getLevel();
        for (int i = 1; i <= getRange(); i++) {
            TileEntity te = world.getBlockEntity(pos);
            if (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getTarget().face).isPresent()) {
                GlobalPos gPos = MiscUtil.makeGlobalPos(world, pos.immutable());
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
        return !MiscUtil.blockHasSolidSide(state, w, pos, face.getOpposite()) || !state.isSolidRender(w, pos);
    }

    static class PositionedItemHandler {
        private final BlockPos pos;
        private final IItemHandler handler;

        static final PositionedItemHandler INVALID = new PositionedItemHandler(null, null);

        PositionedItemHandler(BlockPos pos, IItemHandler handler) {
            this.pos = pos;
            this.handler = handler;
        }

        boolean isValid() {
            return pos != null && handler != null;
        }
    }
}
