package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModuleVacuum;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

public class VacuumModule extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledVacuumModule(router, stack);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        CompiledVacuumModule cvm = new CompiledVacuumModule(null, itemstack);
        if (cvm.getAugmentCount(ItemAugment.AugmentType.XP_VACUUM) > 0) {
            list.add(TextFormatting.YELLOW + I18n.format("guiText.label.xpVacuum") + ": "
                    + TextFormatting.AQUA + I18n.format("guiText.label.xpVacuum." + cvm.getXPCollectionType()));
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

    @Override
    public boolean isOmniDirectional() {
        return true;
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleVacuum.class;
    }

    @Override
    public Color getItemTint() {
        return new Color(120, 48, 191);
    }
}
