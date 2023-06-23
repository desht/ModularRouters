package me.desht.modularrouters.datagen;

import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn.getPackOutput(), Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModularRoutersBlockLoot::new, LootContextParamSets.BLOCK)
        ));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        // TODO
    }

    private static class ModularRoutersBlockLoot extends BlockLootSubProvider {
        public ModularRoutersBlockLoot() {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(ModBlocks.MODULAR_ROUTER.get());
        }

        @Override
        protected void generate() {
            Block router = ModBlocks.MODULAR_ROUTER.get();
            LootPool.Builder builder = LootPool.lootPool()
//                    .name(ModBlocks.MODULAR_ROUTER.getId().getPath())
                    .when(ExplosionCondition.survivesExplosion())
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(router)
                            .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                    .copy("Modules", "BlockEntityTag.Modules", CopyNbtFunction.MergeStrategy.REPLACE)
                                    .copy("Upgrades", "BlockEntityTag.Upgrades", CopyNbtFunction.MergeStrategy.REPLACE)
                                    .copy("Redstone", "BlockEntityTag.Redstone", CopyNbtFunction.MergeStrategy.REPLACE))
                            .apply(SetContainerContents.setContents(ModBlockEntities.MODULAR_ROUTER.get())
                                    .withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS))));
            add(router, LootTable.lootTable().withPool(builder));
        }
    }
}
