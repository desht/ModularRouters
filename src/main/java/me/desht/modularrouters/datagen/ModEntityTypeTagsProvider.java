package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.ModularRoutersTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(DataGenerator dataGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, ModularRouters.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(ModularRoutersTags.EntityTypes.activatorInteractBlacklist);
        tag(ModularRoutersTags.EntityTypes.activatorAttackBlacklist);
    }
}