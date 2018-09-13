package me.desht.modularrouters.item;

import me.desht.modularrouters.client.ModularRoutersTab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import javax.annotation.Nonnull;

public class ItemBase extends Item {
    protected String name;

    public ItemBase(String name) {
        this.name = name;
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(ModularRoutersTab.creativeTab);
    }

    @Nonnull
    @Override
    public ItemBase setCreativeTab(@Nonnull CreativeTabs tab) {
        super.setCreativeTab(tab);
        return this;
    }

    /**
     * Get the number of sub-items this item has.
     *
     * @return the number of sub-items, or 0 if the item doesn't use sub-items
     */
    public int getSubTypes() {
        return 0;
    }

    /**
     * Get the (unlocalized) sub-item name for the given sub-item number.  Used for items models and translations.
     *
     * @param meta item metadata number, in range 0 .. getSubTypes()
     * @return the sub-item name
     */
    public String getSubTypeName(int meta) {
        return name;
    }
}
