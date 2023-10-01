package me.desht.modularrouters.datagen;

import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.function.Consumer;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generator) {
        super(generator.getPackOutput());
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        shaped(ModBlocks.MODULAR_ROUTER.get(), 4, Items.IRON_INGOT,
                "IBI/BMB/IBI",
                'I', Tags.Items.INGOTS_IRON,
                'M', ModItems.BLANK_MODULE.get(),
                'B', Items.IRON_BARS
        ).save(consumer);

        shaped(ModItems.BLANK_MODULE.get(), 6, Items.REDSTONE,
                " R /PPP/GGG",
                'R', Tags.Items.DUSTS_REDSTONE,
                'P', Items.PAPER,
                'G', Tags.Items.NUGGETS_GOLD
        ).save(consumer);

        shaped(ModItems.BLANK_UPGRADE.get(), 4, Items.LAPIS_LAZULI,
                "PPG/PLG/ PG",
                'P', Items.PAPER,
                'L', Tags.Items.GEMS_LAPIS,
                'G', Tags.Items.NUGGETS_GOLD
        ).save(consumer);

        shapeless(ModItems.AUGMENT_CORE.get(), 4, ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), ModItems.BLANK_UPGRADE.get()
        ).save(consumer);

        // augments
        shaped(ModItems.REGULATOR_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                " Q /CMC/ Q ",
                'Q', Tags.Items.GEMS_QUARTZ,
                'M', ModItems.AUGMENT_CORE.get(),
                'C', Items.COMPARATOR
        ).save(consumer);

        shapeless(ModItems.MIMIC_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Tags.Items.OBSIDIAN, Tags.Items.DUSTS_REDSTONE, Tags.Items.DUSTS_GLOWSTONE
        ).save(consumer);

        shaped(ModItems.REDSTONE_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                " T /RMR/ T ",
                'M', ModItems.AUGMENT_CORE.get(),
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', Items.REDSTONE_TORCH
        ).save(consumer);

        shapeless(ModItems.XP_VACUUM_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Items.SOUL_SAND
        ).save(consumer);

        shaped(ModItems.RANGE_DOWN_AUGMENT.get(), 4, ModItems.AUGMENT_CORE.get(),
                " S /QMQ/ Q ",
                'M', ModItems.AUGMENT_CORE.get(),
                'Q', Tags.Items.GEMS_QUARTZ,
                'S', Tags.Items.RODS_WOODEN
        ).save(consumer);

        shapeless(ModItems.RANGE_DOWN_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.RANGE_UP_AUGMENT.get()
        ).save(consumer, RL("range_down_from_up"));

        shaped(ModItems.RANGE_UP_AUGMENT.get(), 4, ModItems.AUGMENT_CORE.get(),
                " Q /QMQ/ S ",
                'S', Tags.Items.RODS_WOODEN,
                'Q', Tags.Items.GEMS_QUARTZ,
                'M', ModItems.AUGMENT_CORE.get()
        ).save(consumer);

        shapeless(ModItems.RANGE_UP_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.RANGE_DOWN_AUGMENT.get()
        ).save(consumer, RL("range_up_from_down"));

        shaped(ModItems.PUSHING_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                "PMP",
                'P', Items.PISTON,
                'M', ModItems.AUGMENT_CORE.get()
        ).save(consumer);

        shapeless(ModItems.PICKUP_DELAY_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Tags.Items.SLIMEBALLS
        ).save(consumer);

        shapeless(ModItems.FAST_PICKUP_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Items.FISHING_ROD
        ).save(consumer);

        shapeless(ModItems.FILTER_ROUND_ROBIN_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Items.CLOCK
        ).save(consumer);

        shapeless(ModItems.STACK_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), ModItems.STACK_UPGRADE.get()
        ).save(consumer);

        // modules
        SpecialRecipeBuilder.special(ModRecipes.EXTRUDER_MODULE_1.get())
                .save(consumer, RL("extruder_module_1").toString());

        shaped(ModItems.FLUID_MODULE.get(), ModItems.BLANK_MODULE.get(),
                " C /GMG",
                'G', Tags.Items.GLASS,
                'C', Items.CAULDRON,
                'M', ModItems.BLANK_MODULE.get()
        ).save(consumer);

        shapeless(ModItems.FLUID_MODULE_2.get(), ModItems.FLUID_MODULE.get(),
                ModItems.FLUID_MODULE.get(), Items.PRISMARINE_SHARD
        ).save(consumer);

        shapeless(ModItems.FLUID_MODULE_2.get(), 4, ModItems.FLUID_MODULE.get(),
                ModItems.FLUID_MODULE.get(), ModItems.FLUID_MODULE.get(),
                ModItems.FLUID_MODULE.get(), ModItems.FLUID_MODULE.get(),
                Items.PRISMARINE_SHARD
        ).save(consumer, RL("fluid_module_2_x4"));

        shapeless(ModItems.SENDER_MODULE_1.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.BOW, ItemTags.ARROWS
        ).save(consumer);

        shapeless(ModItems.SENDER_MODULE_3.get(), ModItems.BLANK_MODULE.get(),
                ModItems.SENDER_MODULE_2.get(), Items.END_STONE, Items.ENDER_CHEST
        ).save(consumer);

        shaped(ModItems.DISTRIBUTOR_MODULE.get(), ModItems.BLANK_MODULE.get(),
                " S /SMS",
                'M', ModItems.BLANK_MODULE.get(),
                'S', ModItems.SENDER_MODULE_2.get()
        ).save(consumer);

        shapeless(ModItems.SENDER_MODULE_2.get(), ModItems.BLANK_MODULE.get(),
                ModItems.SENDER_MODULE_1.get(), Items.ENDER_PEARL
        ).save(consumer);

        SpecialRecipeBuilder.special(ModRecipes.BREAKER_MODULE.get())
                .save(consumer, RL("breaker_module").toString());

        shapeless(ModItems.DROPPER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.DROPPER
        ).save(consumer);

        shapeless(ModItems.EXTRUDER_MODULE_2.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Tags.Items.CHESTS_WOODEN
        ).save(consumer);

        shapeless(ModItems.FLINGER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.DROPPER_MODULE.get(), Items.GUNPOWDER
        ).save(consumer);

        shapeless(ModItems.DETECTOR_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.COMPARATOR
        ).save(consumer);

        shapeless(ModItems.PLACER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.DISPENSER, Items.DIRT
        ).save(consumer);

        shaped(ModItems.PLAYER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                " H /SZP/ C ",
                'C', Items.DIAMOND_CHESTPLATE,
                'P', ModItems.PULLER_MODULE_2.get(),
                'Z', Items.WITHER_SKELETON_SKULL,
                'H', Items.DIAMOND_HELMET,
                'S', ModItems.SENDER_MODULE_3.get()
        ).save(consumer);

        shapeless(ModItems.PULLER_MODULE_1.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.STICKY_PISTON
        ).save(consumer);

        shapeless(ModItems.SENDER_MODULE_2.get(), 4, ModItems.BLANK_MODULE.get(),
                ModItems.SENDER_MODULE_1.get(),
                ModItems.SENDER_MODULE_1.get(),
                ModItems.SENDER_MODULE_1.get(),
                ModItems.SENDER_MODULE_1.get(),
                Items.ENDER_PEARL
        ).save(consumer, RL("sender_module_2_x4"));

        shapeless(ModItems.PULLER_MODULE_2.get(), 4, ModItems.BLANK_MODULE.get(),
                ModItems.PULLER_MODULE_1.get(), ModItems.PULLER_MODULE_1.get(), ModItems.PULLER_MODULE_1.get(), ModItems.PULLER_MODULE_1.get(), Items.ENDER_PEARL
        ).save(consumer, RL("puller_module_2_x4"));

        shapeless(ModItems.VACUUM_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.HOPPER, Items.ENDER_EYE
        ).save(consumer);

        shaped(ModItems.ACTIVATOR_MODULE.get(), ModItems.BLANK_MODULE.get(),
                "RLR/DMD/RQR",
                'Q', Tags.Items.GEMS_QUARTZ,
                'D', Items.DISPENSER,
                'R', Tags.Items.DUSTS_REDSTONE,
                'M', ModItems.BLANK_MODULE.get(),
                'L', Items.LEVER
        ).save(consumer);

        shapeless(ModItems.PULLER_MODULE_2.get(), ModItems.BLANK_MODULE.get(),
                ModItems.PULLER_MODULE_1.get(), Items.ENDER_PEARL
        ).save(consumer);

        shapeless(ModItems.VOID_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.LAVA_BUCKET
        ).save(consumer);

        shapeless(ModItems.SENDER_MODULE_1.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.PISTON
        ).save(consumer, RL("sender_module_1_alt"));

        // upgrades
        shaped(ModItems.MUFFLER_UPGRADE.get(), 4, ModItems.BLANK_UPGRADE.get(),
                " W /WBW/ W ",
                'W', ItemTags.WOOL,
                'B', ModItems.BLANK_UPGRADE.get()
        ).save(consumer);

        shaped(ModItems.SECURITY_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                " Q /NBN/ R ",
                'B', ModItems.BLANK_UPGRADE.get(),
                'N', Tags.Items.NUGGETS_GOLD,
                'R', Tags.Items.DUSTS_REDSTONE,
                'Q', Tags.Items.GEMS_QUARTZ
        ).save(consumer);

        shaped(ModItems.SPEED_UPGRADE.get(), 3, ModItems.BLANK_UPGRADE.get(),
                "RIR/NBN/GZG",
                'I', Tags.Items.INGOTS_GOLD,
                'G', Tags.Items.GUNPOWDER,
                'R', Tags.Items.DUSTS_REDSTONE,
                'B', ModItems.BLANK_UPGRADE.get(),
                'N', Tags.Items.NUGGETS_GOLD,
                'Z', Tags.Items.RODS_BLAZE
        ).save(consumer);

        shaped(ModItems.BLAST_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                "IOI/OBO/IOI",
                'O', Tags.Items.OBSIDIAN,
                'I', Items.IRON_BARS,
                'B', ModItems.BLANK_UPGRADE.get()
        ).save(consumer);

        shapeless(ModItems.CAMOUFLAGE_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                ModItems.BLANK_UPGRADE.get(), Tags.Items.DYES_RED, Tags.Items.DYES_GREEN, Tags.Items.DYES_BLUE
        ).save(consumer);

        shaped(ModItems.FLUID_UPGRADE.get(), 3, ModItems.BLANK_UPGRADE.get(),
                " U /GBG",
                'G', Tags.Items.GLASS,
                'U', Items.BUCKET,
                'B', ModItems.BLANK_UPGRADE.get()
        ).save(consumer);

        shaped(ModItems.SYNC_UPGRADE.get(), 16, ModItems.BLANK_UPGRADE.get(),
                "RST/RBR",
                'S', Tags.Items.STONE,
                'T', Items.REDSTONE_TORCH,
                'B', ModItems.BLANK_UPGRADE.get(),
                'R', Tags.Items.DUSTS_REDSTONE
        ).save(consumer);

        shapeless(ModItems.STACK_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                ModItems.BLANK_UPGRADE.get(), ItemTags.STONE_BRICKS, Tags.Items.INGOTS_BRICK
        ).save(consumer);

        shaped(ModItems.ENERGY_OUTPUT_MODULE.get(), ModItems.BLANK_MODULE.get(),
                " R /GBG/ Q ",
                'R', Tags.Blocks.STORAGE_BLOCKS_REDSTONE,
                'B', ModItems.BLANK_MODULE.get(),
                'G', Tags.Items.INGOTS_GOLD,
                'Q', Tags.Items.GEMS_QUARTZ
        ).save(consumer);

        shapeless(ModItems.ENERGY_DISTRIBUTOR_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.ENERGY_OUTPUT_MODULE.get(), ModItems.DISTRIBUTOR_MODULE.get()
        ).save(consumer);

        shaped(ModItems.ENERGY_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                "QRQ/ B /QGQ",
                'R', Tags.Blocks.STORAGE_BLOCKS_REDSTONE,
                'B', ModItems.BLANK_UPGRADE.get(),
                'G', Tags.Items.INGOTS_GOLD,
                'Q', Tags.Items.GEMS_QUARTZ
        ).save(consumer);

        // filters
        shaped(ModItems.BULK_ITEM_FILTER.get(), ModItems.BLANK_MODULE.get(),
                "IGI/MDM/IGI",
                'G', Tags.Items.GLASS,
                'D', Tags.Items.GEMS_DIAMOND,
                'I', Tags.Items.INGOTS_IRON,
                'M', ModItems.BLANK_MODULE.get()
        ).save(consumer);

        shapeless(ModItems.MOD_FILTER.get(), ModItems.BULK_ITEM_FILTER.get(),
                ModItems.BULK_ITEM_FILTER.get(), Items.REPEATER, Items.REDSTONE_TORCH
        ).save(consumer);

        shapeless(ModItems.TAG_FILTER.get(), ModItems.BULK_ITEM_FILTER.get(),
                ModItems.BULK_ITEM_FILTER.get(), Items.PAPER, Ingredient.of(Tags.Items.DYES_BLACK)
        ).save(consumer);

        shaped(ModItems.INSPECTION_FILTER.get(), ModItems.BULK_ITEM_FILTER.get(),
                "EBE/ P ",
                'B', ModItems.BULK_ITEM_FILTER.get(),
                'P', Items.PAPER,
                'E', Items.SPIDER_EYE
        ).save(consumer);

        shapeless(ModItems.REGEX_FILTER.get(), ModItems.BULK_ITEM_FILTER.get(),
                ModItems.BULK_ITEM_FILTER.get(), Items.COMPARATOR
        ).save(consumer);

        SpecialRecipeBuilder.special(ModRecipes.MODULE_RESET.get()).save(consumer, RL("reset_module").toString());
        ConditionalRecipe.builder()
                .addCondition(new ModLoadedCondition("patchouli"))
                .addRecipe(c -> SpecialRecipeBuilder.special(ModRecipes.GUIDE_BOOK.get()).save(c, RL("guide_book").toString()))
                .build(consumer, RL("guide_book"));
    }

    private <T extends ItemLike> ShapedRecipeBuilder shaped(T result, T required, String pattern, Object... keys) {
        return shaped(result, 1, required, pattern, keys);
    }

    private <T extends ItemLike> ShapedRecipeBuilder shaped(T result, int count, T required, String pattern, Object... keys) {
        ShapedRecipeBuilder b = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, count);
        Arrays.stream(pattern.split("/")).forEach(b::pattern);
        for (int i = 0; i < keys.length; i += 2) {
            Object v = keys[i + 1];
            if (v instanceof TagKey<?>) {
                //noinspection unchecked
                b.define((Character) keys[i], (TagKey<Item>) v);
            } else if (v instanceof ItemLike) {
                b.define((Character) keys[i], (ItemLike) v);
            } else if (v instanceof Ingredient) {
                b.define((Character) keys[i], (Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.unlockedBy("has_" + safeName(required), has(required));
        return b;
    }

    private <T extends ItemLike> ShapelessRecipeBuilder shapeless(T result, T required, Object... ingredients) {
        return shapeless(result, 1, required, ingredients);
    }

    private <T extends ItemLike> ShapelessRecipeBuilder shapeless(T result, int count, T required, Object... ingredients) {
        ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result, count);
        for (Object v : ingredients) {
            if (v instanceof TagKey<?>) {
                //noinspection unchecked
                b.requires((TagKey<Item>) v);
            } else if (v instanceof ItemLike) {
                b.requires((ItemLike) v);
            } else if (v instanceof Ingredient) {
                b.requires((Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.unlockedBy("has_" + safeName(required), has(required));
        return b;
    }

    private <T extends ItemLike> String safeName(T required) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(required.asItem());
        return key == null ? "" : key.getPath().replace('/', '_');
    }
}
