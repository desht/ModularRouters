package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DistributorModule extends SenderModule2 {

    private static final TintColor TINT_COLOR = new TintColor(240, 240, 60);

    public DistributorModule() {
        super(CompiledDistributorModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledDistributorModule cdm = new CompiledDistributorModule(null, itemstack);
        list.add(ClientUtil.xlate("modularrouters.guiText.tooltip.distributor.strategy").append(": ").withStyle(TextFormatting.YELLOW)
                .append(ClientUtil.xlate(cdm.getDistributionStrategy().getTranslationKey())).withStyle(TextFormatting.AQUA));
        list.add(ClientUtil.xlate("modularrouters.itemText.fluid.direction." + (cdm.isPulling() ? "IN" : "OUT")).withStyle(TextFormatting.YELLOW));
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_DISTRIBUTOR.get();
    }

    @Override
    public List<ModuleTarget> getStoredPositions(@Nonnull ItemStack stack) {
        Set<ModuleTarget> targets = TargetedModule.getTargets(stack, false);
        return new ArrayList<>(targets);
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    protected int getMaxTargets() {
        return 8;
    }

    @Override
    public int getRenderColor(int index) {
        return 0x80B0FF90;
    }
}
