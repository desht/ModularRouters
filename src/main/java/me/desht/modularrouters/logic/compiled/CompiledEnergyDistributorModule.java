package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CompiledEnergyDistributorModule extends CompiledModule {
    protected final CompiledDistributorModule.DistributorSettings settings;

    public CompiledEnergyDistributorModule(@Nullable ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.DISTRIBUTOR_SETTINGS, CompiledDistributorModule.DistributorSettings.DEFAULT);
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        if (!getTargets().isEmpty()) {
            IEnergyStorage storage = router.getEnergyStorage();
            if (storage != null) {
                boolean doBeam = router.getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2;
                return settings.isPulling() ?
                        pullEnergy(router, storage, getTargets(), doBeam) > 0 :
                        sendEnergy(router, storage, getTargets(), doBeam) > 0;
            }
        }
        return false;
    }

    private int sendEnergy(@Nonnull ModularRouterBlockEntity router, IEnergyStorage storage, List<ModuleTarget> targets, boolean doBeam) {
        int total = 0;
        int toSend = storage.getEnergyStored() / targets.size();

        for (ModuleTarget target : targets) {
            total += target.getEnergyHandler().map(handler -> {
                int toExtract = storage.extractEnergy(toSend, true);
                int sent = handler.receiveEnergy(toExtract, false);
                storage.extractEnergy(sent, false);
                if (sent > 0 && doBeam) {
                    router.addItemBeam(new BeamData.Builder(router, target.gPos.pos(), 0xE0FF4040).build());
                }
                return sent;
            }).orElse(0);
        }
        return total;
    }

    private int pullEnergy(@Nonnull ModularRouterBlockEntity router, IEnergyStorage storage, List<ModuleTarget> targets, boolean doBeam) {
        int total = 0;
        int toPull = (storage.getMaxEnergyStored() - storage.getEnergyStored()) / targets.size();

        for (ModuleTarget target : targets) {
            total += target.getEnergyHandler().map(handler -> {
                int toExtract = handler.extractEnergy(toPull, true);
                int received = storage.receiveEnergy(toExtract, false);
                handler.extractEnergy(received, false);
                if (received > 0 && doBeam) {
                    router.addItemBeam(new BeamData.Builder(router, target.gPos.pos(), 0xE0C040A0).reversed(true).build());
                }
                return received;
            }).orElse(0);
        }
        return total;
    }

    public boolean isPulling() {
        return settings.isPulling();
    }
}
