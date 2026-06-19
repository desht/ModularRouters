package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.ModularRoutersTags;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider) {
        super(output, lookupProvider, ModularRouters.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
            if (holder.get() instanceof ModuleItem) {
                addItemsToTag(ModularRoutersTags.Items.MODULES, holder);
            } else if (holder.get() instanceof UpgradeItem) {
                addItemsToTag(ModularRoutersTags.Items.UPGRADES, holder);
            } else if (holder.get() instanceof AugmentItem) {
                addItemsToTag(ModularRoutersTags.Items.AUGMENTS, holder);
            } else if (holder.get() instanceof SmartFilterItem) {
                addItemsToTag(ModularRoutersTags.Items.FILTERS, holder);
            }
        }

        tag(ModularRoutersTags.Items.ACTIVATOR_BLACKLIST);//.addOptional(ResourceLocation.parse("notenoughwands:acceleration_wand"));
        tag(ModularRoutersTags.Items.PLAYER_MODULE_BLACKLIST);
    }

    @SafeVarargs
    private void addItemsToTag(TagKey<Item> tagKey, DeferredHolder<Item, ? extends Item>... items) {
        @SuppressWarnings("unchecked")
        ResourceKey<Item>[] keys = Arrays.stream(items).map(DeferredHolder::getKey).toArray(ResourceKey[]::new);
        tag(tagKey).add(keys);
    }

    @Override
    public String getName() {
        return "Modular Routers Item Tags";
    }

}
