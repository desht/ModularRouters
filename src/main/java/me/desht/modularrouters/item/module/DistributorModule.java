package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModuleDistributor;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DistributorModule extends SenderModule2 {
    public DistributorModule(Properties props) {
        super(props);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);
        CompiledDistributorModule cdm = new CompiledDistributorModule(null, itemstack);

        list.add(new TextComponentString(
                TextFormatting.YELLOW + I18n.format("guiText.tooltip.distributor.strategy") + ": "
                + TextFormatting.AQUA + I18n.format("itemText.distributor.strategy." + cdm.getDistributionStrategy()))
        );
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledDistributorModule(router, stack);
    }

    @Override
    public List<ModuleTarget> getStoredPositions(@Nonnull ItemStack stack) {
        Set<ModuleTarget> targets = TargetedModule.getTargets(stack, false);
        return new ArrayList<>(targets);
    }

    @Override
    public Color getItemTint() {
        return new Color(240, 240, 60);
    }

    @Override
    public Class<? extends GuiModule> getGuiClass() {
        return GuiModuleDistributor.class;
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