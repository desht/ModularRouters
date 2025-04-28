package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.ModularRoutersTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ModularRouters.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModularRoutersTags.EntityTypes.activatorInteractBlacklist).add(EntityType.VILLAGER, EntityType.WANDERING_TRADER);
        tag(ModularRoutersTags.EntityTypes.activatorAttackBlacklist);
    }
}
