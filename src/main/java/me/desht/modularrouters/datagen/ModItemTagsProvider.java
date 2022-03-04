package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.ModularRoutersTags;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.function.Supplier;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(DataGenerator generatorIn, ExistingFileHelper existingFileHelper) {
        super(generatorIn, new BlockTagsProvider(generatorIn, ModularRouters.MODID, existingFileHelper), ModularRouters.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        for (RegistryObject<Item> ro : ModItems.ITEMS.getEntries()) {
            if (ro.get() instanceof ModuleItem) {
                addItemsToTag(ModularRoutersTags.Items.MODULES, ro);
            } else if (ro.get() instanceof UpgradeItem) {
                addItemsToTag(ModularRoutersTags.Items.UPGRADES, ro);
            } else if (ro.get() instanceof AugmentItem) {
                addItemsToTag(ModularRoutersTags.Items.AUGMENTS, ro);
            } else if (ro.get() instanceof SmartFilterItem) {
                addItemsToTag(ModularRoutersTags.Items.FILTERS, ro);
            }
        }
    }

    @SafeVarargs
    private void addItemsToTag(TagKey<Item> tagKey, Supplier<? extends ItemLike>... items) {
        tag(tagKey).add(Arrays.stream(items).map(Supplier::get).map(ItemLike::asItem).toArray(Item[]::new));
    }

    @Override
    public String getName() {
        return "Modular Routers Item Tags";
    }
}
