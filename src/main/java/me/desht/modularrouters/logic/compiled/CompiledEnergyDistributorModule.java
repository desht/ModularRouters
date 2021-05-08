package me.desht.modularrouters.logic.compiled;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ItemBeamMessage;
import me.desht.modularrouters.network.PacketHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CompiledEnergyDistributorModule extends CompiledModule {
    public CompiledEnergyDistributorModule(@Nullable TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        Vector3d vec = Vector3d.atCenterOf(router.getBlockPos());
        PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(vec.x, vec.y, vec.z, 32, router.getLevel().dimension());

        List<ModuleTarget> inRange = getTargets().stream()
                .filter(target -> target.isSameWorld(router.getLevel()) && router.getBlockPos().distSqr(target.gPos.pos()) <= getRangeSquared())
                .collect(Collectors.toList());
        if (inRange.isEmpty()) return false;

        int total = router.getCapability(CapabilityEnergy.ENERGY).map(routerHandler -> {
            int toSend = routerHandler.getEnergyStored() / inRange.size();
            int total1 = 0;
            boolean beam = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2;
            for (ModuleTarget target : inRange) {
                total1 += target.getEnergyHandler().map(handler -> {
                    int toExtract = routerHandler.extractEnergy(toSend, true);
                    int sent = handler.receiveEnergy(toExtract, false);
                    routerHandler.extractEnergy(sent, false);
                    if (sent > 0 && beam) {
                        PacketHandler.NETWORK.send(PacketDistributor.NEAR.with(() -> tp),
                                new ItemBeamMessage(router, target.gPos.pos(), false, ItemStack.EMPTY, 0xE04040, router.getTickRate(), false));
                    }
                    return sent;
                }).orElse(0);
            }
            return total1;
        }).orElse(0);

        return total > 0;
    }

    @Override
    public List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        return router == null ? Collections.emptyList() : ImmutableList.copyOf(TargetedModule.getTargets(stack, !router.getLevel().isClientSide));
    }
}
