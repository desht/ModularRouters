package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.ModularRoutersTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(DataGenerator dataGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator.getPackOutput(), lookupProvider, ModularRouters.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModularRoutersTags.EntityTypes.activatorInteractBlacklist).add(EntityType.VILLAGER, EntityType.WANDERING_TRADER);
        tag(ModularRoutersTags.EntityTypes.activatorAttackBlacklist);
    }
}
