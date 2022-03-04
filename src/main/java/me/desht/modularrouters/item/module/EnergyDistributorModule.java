package me.desht.modularrouters.item.module;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledEnergyDistributorModule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.List;

public class EnergyDistributorModule extends TargetedModule implements IRangedModule, IPositionProvider {
    private static final TintColor TINT_COLOR = new TintColor(54, 1, 61);

    public EnergyDistributorModule() {
        super(ModItems.defaultProps(), CompiledEnergyDistributorModule::new);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(79, 9, 90);
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.energydistributorModuleEnergyCost.get();
    }

    @Override
    public int getBaseRange() {
        return 8;
    }

    @Override
    public int getHardMaxRange() {
        return 48;
    }

    @Override
    public List<ModuleTarget> getStoredPositions(@Nonnull ItemStack stack) {
        return ImmutableList.copyOf(TargetedModule.getTargets(stack, false));
    }

    @Override
    protected boolean isValidTarget(UseOnContext ctx) {
        BlockEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        return te != null && te.getCapability(CapabilityEnergy.ENERGY, ctx.getClickedFace())
                .map(IEnergyStorage::canReceive)
                .orElse(false);
    }

    @Override
    protected int getMaxTargets() {
        return 8;
    }

    @Override
    public int getRenderColor(int index) {
        return 0x80E08080;
    }
}
