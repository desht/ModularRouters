package me.desht.modularrouters.block;

import me.desht.modularrouters.client.ModularRoutersTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

class BlockBase extends Block {
    BlockBase(Material material, String name) {
        super(material);

        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(ModularRoutersTab.creativeTab);
    }
}
