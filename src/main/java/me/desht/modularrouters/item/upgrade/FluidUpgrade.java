package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.item.ModItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;

public class FluidUpgrade extends Upgrade {
    @Override
    public Object[] getExtraUsageParams() {
        return new Object[] { ConfigHandler.router.mBperFluidUpgrade };
    }

    @Override
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addUsageInformation(itemstack, player, list, par4);
        TileEntityItemRouter router = ModularRouters.proxy.getOpenItemRouter();
        if (router != null) {
            list.add(I18n.format("itemText.usage.item.fluidUpgradeRouter", router.getFluidTransferRate()));
        }
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapedOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.FLUID, 3),
                " b ", "gug",
                'u', ModItems.blankUpgrade, 'b', Items.BUCKET, 'g', Blocks.GLASS);
    }
}
