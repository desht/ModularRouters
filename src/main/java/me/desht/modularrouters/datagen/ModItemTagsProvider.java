package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRoutersTags;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.fml.RegistryObject;

import java.util.Arrays;
import java.util.function.Supplier;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerTags() {
        for (RegistryObject<Item> ro : ModItems.ITEMS.getEntries()) {
            if (ro.get() instanceof ItemModule) {
                addItemsToTag(ModularRoutersTags.Items.MODULES, ro);
            } else if (ro.get() instanceof ItemUpgrade) {
                addItemsToTag(ModularRoutersTags.Items.UPGRADES, ro);
            } else if (ro.get() instanceof ItemAugment) {
                addItemsToTag(ModularRoutersTags.Items.AUGMENTS, ro);
            } else if (ro.get() instanceof ItemSmartFilter) {
                addItemsToTag(ModularRoutersTags.Items.FILTERS, ro);
            }
        }
    }

    @SafeVarargs
    private final void addItemsToTag(Tag<Item> tag, Supplier<? extends IItemProvider>... items) {
        getBuilder(tag).add(Arrays.stream(items).map(Supplier::get).map(IItemProvider::asItem).toArray(Item[]::new));
    }

    @Override
    public String getName() {
        return "Modular Routers Item Tags";
    }
}
