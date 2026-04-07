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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class CompiledSenderModule1 extends CompiledModule {
    public CompiledSenderModule1(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(ModularRouterBlockEntity router) {
        ResourceHandler<ItemResource> buffer = router.getBuffer();
        ItemStack bufferStack = ItemUtil.getStack(buffer, 0);
        if (getFilter().test(bufferStack)) {
            return findTargetInventory(router).map(positionedItemHandler -> {
                int nToSend = getItemsPerTick(router);
                if (getRegulationAmount() > 0) {
                    int existing = InventoryUtils.countItems(bufferStack, positionedItemHandler.handler, getRegulationAmount(), !getFilter().getFlags().matchDamage());
                    nToSend = Math.min(nToSend, getRegulationAmount() - existing);
                    if (nToSend <= 0) {
                        return false;
                    }
                }
                int sent = InventoryUtils.transferItems(buffer, positionedItemHandler.handler, 0, nToSend);
                if (sent > 0) {
                    if (ConfigHolder.common.module.senderParticles.get()) {
                        playParticles(router, positionedItemHandler.pos, bufferStack.copyWithCount(sent));
                    }
                    return true;
                } else {
                    return false;
                }
            }).orElse(false);
        }
        return false;
    }

    void playParticles(ModularRouterBlockEntity router, BlockPos targetPos, ItemStack stack) {
        if (router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2) {
            router.addItemBeam(new BeamData.Builder(router, targetPos, getBeamColor()).withItemStack(stack).build());
        }
    }

    protected int getBeamColor() {
        return 0xFFC000;
    }

    protected Optional<PositionedItemHandler> findTargetInventory(ModularRouterBlockEntity router) {
        return getEffectiveTarget(router).flatMap(target ->
                target.getItemHandler().map(h -> new PositionedItemHandler(target.gPos.pos(), h)));
    }

    @Override
    public Optional<ModuleTarget> getEffectiveTarget(ModularRouterBlockEntity router) {
        if (getAbsoluteFacing() == null) {
            return Optional.empty();
        }

        return getTarget().map(target -> {
            BlockPos.MutableBlockPos pos = target.gPos.pos().mutable();
            Direction face = target.face;
            Level level = router.nonNullLevel();
            for (int i = 1; i <= getRange(); i++) {
                if (level.getCapability(Capabilities.Item.BLOCK, pos, target.face) != null) {
                    return Optional.of(new ModuleTarget(GlobalPos.of(level.dimension(), pos.immutable()), face, BlockUtil.getBlockName(level, pos)));
                } else if (!isPassable(level, pos, face)) {
                    return Optional.<ModuleTarget>empty();
                }
                pos.move(getAbsoluteFacing());
            }
            return Optional.<ModuleTarget>empty();
        }).orElse(Optional.empty());
    }

    private boolean isPassable(Level w, BlockPos pos, Direction face) {
        BlockState state = w.getBlockState(pos);
        return !MiscUtil.blockHasSolidSide(state, w, pos, face.getOpposite()) || !state.isSolidRender();
    }

    public record PositionedItemHandler(BlockPos pos, ResourceHandler<ItemResource> handler) {
    }
}
