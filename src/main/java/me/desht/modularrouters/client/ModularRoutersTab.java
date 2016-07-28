package me.desht.modularrouters.client;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ModularRoutersTab extends CreativeTabs {

    public static final ModularRoutersTab creativeTab = new ModularRoutersTab();

    public ModularRoutersTab() {
        super(ModularRouters.modId);
    }

    @Override
    public Item getTabIconItem() {
        return ModItems.blankModule;
    }
}
