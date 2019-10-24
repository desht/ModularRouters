package me.desht.modularrouters.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class TagOwnerTracker implements ISelectiveResourceReloadListener {
    private static TagOwnerTracker INSTANCE;
    private final Map<ResourceLocation, Set<ResourceLocation>> cache = new HashMap<>();

    private TagOwnerTracker() { }

    public static TagOwnerTracker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TagOwnerTracker();
            ServerLifecycleHooks.getCurrentServer().getResourceManager().addReloadListener(INSTANCE);
        }
        return INSTANCE;
    }

    public <T extends IForgeRegistryEntry> Set<ResourceLocation> getOwningTags(TagCollection<T> collection, T t) {
        return cache.computeIfAbsent(t.getRegistryName(), resourceLocation -> {
            Set<ResourceLocation> res = Sets.newHashSet();
            collection.getTagMap().forEach((resLoc, value) -> {
                if (value.contains(t)) {
                    res.add(resLoc);
                }
            });
            return ImmutableSet.copyOf(res);
        });
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        cache.clear();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        onResourceManagerReload(resourceManager);
    }

    public static Set<ResourceLocation> getItemTags(ItemStack stack) {
        return getInstance().getOwningTags(ItemTags.getCollection(), stack.getItem());
    }
}
