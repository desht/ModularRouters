package me.desht.modularrouters.block;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.ModularRoutersTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

public class BlockBase extends Block {

    protected String name;

    public BlockBase(Material material, String name) {
        super(material);

        this.name = name;

        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(ModularRoutersTab.creativeTab);
    }

    public void registerItemModel(ItemBlock itemBlock) {
        ModularRouters.proxy.registerItemRenderer(itemBlock, 0, name);
    }

    @Override
    public BlockBase setCreativeTab(CreativeTabs tab) {
        super.setCreativeTab(tab);
        return this;
    }

}
