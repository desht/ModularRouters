package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModuleDistributor;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DistributorModule extends SenderModule2 {
    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        CompiledDistributorModule cdm = new CompiledDistributorModule(null, itemstack);
        list.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip.distributor.strategy") + ": "
                + TextFormatting.AQUA + I18n.format("itemText.distributor.strategy." + cdm.getDistributionStrategy()));
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
    public Class<? extends GuiModule> getGuiHandler() {
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
