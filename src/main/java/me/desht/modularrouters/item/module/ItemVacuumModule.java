package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.VacuumExecutor;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemVacuumModule extends AbstractModule {
    public ItemVacuumModule() {
        super("vacuumModule");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new VacuumExecutor();
    }

    public static int getVacuumRange(TileEntityItemRouter router) {
        return Math.min(Config.vacuumBaseRange + router.getRangeUpgrades(), Config.vacuumMaxRange);
    }

    @Override
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + getUnlocalizedName(itemstack), Config.Defaults.VACUUM_BASE_RANGE, Config.Defaults.VACUUM_MAX_RANGE);
    }
}
