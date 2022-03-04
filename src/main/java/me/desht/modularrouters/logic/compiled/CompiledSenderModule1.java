package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import me.desht.modularrouters.util.BlockUtil;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class CompiledSenderModule1 extends CompiledModule {
    public CompiledSenderModule1(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
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
                    if (ConfigHolder.common.module.senderParticles.get()) {
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

    void playParticles(ModularRouterBlockEntity router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            router.addItemBeam(new BeamData(router.getTickRate(), targetPos, stack, getBeamColor()));
        }
    }

    protected int getBeamColor() {
        return 0xFFC000;
    }

    PositionedItemHandler findTargetInventory(ModularRouterBlockEntity router) {
        ModuleTarget target = getEffectiveTarget(router);
        if (target != null) {
            return target.getItemHandler().map(h -> new PositionedItemHandler(target.gPos.pos(), h)).orElse(PositionedItemHandler.INVALID);
        }
        return PositionedItemHandler.INVALID;
    }

    @Override
    public ModuleTarget getEffectiveTarget(ModularRouterBlockEntity router) {
        BlockPos p0 = getTarget().gPos.pos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(p0.getX(), p0.getY(), p0.getZ());
        Direction face = getTarget().face;
        Level world = router.getLevel();
        for (int i = 1; i <= getRange(); i++) {
            BlockEntity te = world.getBlockEntity(pos);
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

    private boolean isPassable(Level w, BlockPos pos, Direction face) {
        BlockState state = w.getBlockState(pos);
        return !MiscUtil.blockHasSolidSide(state, w, pos, face.getOpposite()) || !state.isSolidRender(w, pos);
    }

    record PositionedItemHandler(BlockPos pos, IItemHandler handler) {
        static final PositionedItemHandler INVALID = new PositionedItemHandler(null, null);

        boolean isValid() {
            return pos != null && handler != null;
        }
    }
}
