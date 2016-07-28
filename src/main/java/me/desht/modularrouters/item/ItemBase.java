package me.desht.modularrouters.item;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.ModularRoutersTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBase extends Item {
    protected String name;

    public ItemBase(String name) {
        this.name = name;
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(ModularRoutersTab.creativeTab);
    }

    public void registerItemModel(int nSubtypes) {
        if (nSubtypes == 0) {
            ModularRouters.proxy.registerItemRenderer(this, 0, name);
        } else {
            for (int i = 0; i < nSubtypes; i++) {
                ModularRouters.proxy.registerItemRenderer(this, i, getSubTypeName(i));
            }
        }
    }

    @Override
    public ItemBase setCreativeTab(CreativeTabs tab) {
        super.setCreativeTab(tab);
        return this;
    }

    public String getSubTypeName(int meta) {
        return name;
    }
}
