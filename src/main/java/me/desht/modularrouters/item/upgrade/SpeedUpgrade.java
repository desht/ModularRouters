package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class SpeedUpgrade extends Upgrade {
    @Override
    public Object[] getExtraUsageParams() {
        int maxUseful = (int) Math.ceil((Config.baseTickRate - Config.hardMinTickRate) / (double) Config.ticksPerUpgrade);
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        float tickRate = router == null ? 1.0f : router.getTickRate() / 20.0f;
        return new Object[] { tickRate, maxUseful };
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.SPEED),
                ModItems.blankUpgrade, Items.BLAZE_POWDER, Items.SUGAR, Items.GUNPOWDER, Items.REDSTONE);
    }
}
