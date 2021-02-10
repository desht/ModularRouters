package me.desht.modularrouters.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyName;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.SetContents;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(
                Pair.of(BlockLootTable::new, LootParameterSets.BLOCK)
        );
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        // TODO
    }

    private static class BlockLootTable extends BlockLootTables {
        @Override
        protected void addTables() {
            Block router = ModBlocks.ITEM_ROUTER.get();
            LootPool.Builder builder = LootPool.builder()
                    .name(router.getRegistryName().getPath())
                    .acceptCondition(SurvivesExplosion.builder())
                    .rolls(ConstantRange.of(1))
                    .addEntry(ItemLootEntry.builder(router)
                            .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
                            .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
                                    .addOperation("Modules", "BlockEntityTag.Modules", CopyNbt.Action.REPLACE)
                                    .addOperation("Upgrades", "BlockEntityTag.Upgrades", CopyNbt.Action.REPLACE)
                                    .addOperation("Redstone", "BlockEntityTag.Redstone", CopyNbt.Action.REPLACE))
                            .acceptFunction(SetContents.builderIn()
                                    .addLootEntry(DynamicLootEntry.func_216162_a(ShulkerBoxBlock.CONTENTS))));
            registerLootTable(router, LootTable.builder().addLootPool(builder));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return Collections.singletonList(ModBlocks.ITEM_ROUTER.get());
        }
    }
}
