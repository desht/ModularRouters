package me.desht.modularrouters.client;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.RegistrarMR;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ModularRoutersTab extends CreativeTabs {

    public static final ModularRoutersTab creativeTab = new ModularRoutersTab();

    public ModularRoutersTab() {
        super(ModularRouters.MODID);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(RegistrarMR.ITEM_ROUTER);
    }
}
