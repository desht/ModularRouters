package me.desht.modularrouters.datagen;

import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModularRoutersBlockLoot::new, LootContextParamSets.BLOCK)),
                lookupProvider);
    }

    @Override
    protected void validate(WritableRegistry<LootTable> tables, ValidationContextSource validationContext, ProblemReporter.Collector problems) {
        // TODO
    }

    private static class ModularRoutersBlockLoot extends BlockLootSubProvider {
        public ModularRoutersBlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.DEFAULT_FLAGS, provider);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(ModBlocks.MODULAR_ROUTER.get());
        }

        @Override
        protected void generate() {
            Block router = ModBlocks.MODULAR_ROUTER.get();
            LootPool.Builder builder = LootPool.lootPool()
                    .when(ExplosionCondition.survivesExplosion())
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(router)
                            .apply(CopyNameFunction.copyName(LootContext.BlockEntityTarget.BLOCK_ENTITY))
                            .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                    .include(ModDataComponents.REDSTONE_BEHAVIOUR.get())
                                    .include(ModDataComponents.SAVED_MODULES.get())
                                    .include(ModDataComponents.SAVED_UPGRADES.get())
                            )
                    );
            add(router, LootTable.lootTable().withPool(builder));
        }
    }
}
