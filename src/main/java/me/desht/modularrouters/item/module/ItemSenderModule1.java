package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.logic.execution.Sender1Executor;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemSenderModule1 extends AbstractModule {
    public ItemSenderModule1() {
        super("senderModule1");
    }

    @Override
    public ModuleExecutor getExecutor() {
        return new Sender1Executor();
    }

    public static int maxDistance(TileEntityItemRouter router) {
        return Config.sender1BaseRange + Math.min(router.getRangeUpgrades(), Config.sender1BaseRange);
    }

    @Override
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + getUnlocalizedName(itemstack), Config.Defaults.SENDER1_BASE_RANGE, Config.Defaults.SENDER1_MAX_RANGE);
    }
}
