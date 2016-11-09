package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class SpeedUpgrade extends Upgrade {
    @Override
    public Object[] getExtraUsageParams() {
        int maxUseful = (int) Math.ceil((Config.baseTickRate - Config.hardMinTickRate) / (double) Config.ticksPerUpgrade);
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        int tickRate = router == null ? 20 : router.getTickRate();
        return new Object[] { tickRate / 20.0f, tickRate, maxUseful };
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.SPEED),
                "pnp", "nbn", "grg",
                'p', Items.REDSTONE, 'b', ModItems.blankUpgrade, 'r', Items.BLAZE_ROD,
                'g', Items.GUNPOWDER, 'n', Items.GOLD_NUGGET);
    }
}
