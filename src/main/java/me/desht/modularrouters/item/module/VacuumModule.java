package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class VacuumModule extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVacuumModule(router, stack);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        if (ModuleHelper.hasFastPickup(itemstack)) {
            list.add(TextFormatting.GREEN + I18n.format("itemText.misc.fastPickup"));
        }
        if (ModuleHelper.hasXPVacuum(itemstack)) {
            list.add(TextFormatting.GREEN + I18n.format("itemText.misc.xpVacuum"));
        }
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.module.vacuumBaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.module.vacuumMaxRange;
    }
}
