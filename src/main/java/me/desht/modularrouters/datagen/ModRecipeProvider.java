package me.desht.modularrouters.datagen;

import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Arrays;
import java.util.function.Consumer;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        shaped(ModBlocks.ITEM_ROUTER.get(), 4, Items.IRON_INGOT,
                "IBI/BMB/IBI",
                'I', Tags.Items.INGOTS_IRON,
                'M', ModItems.BLANK_MODULE.get(),
                'B', Items.IRON_BARS
        ).build(consumer);

        shaped(ModItems.BLANK_MODULE.get(), 6, Items.REDSTONE,
                " R /PPP/GGG",
                'R', Tags.Items.DUSTS_REDSTONE,
                'P', Items.PAPER,
                'G', Tags.Items.NUGGETS_GOLD
        ).build(consumer);

        shaped(ModItems.BLANK_UPGRADE.get(), 4, Items.LAPIS_LAZULI,
                "PPG/PLG/ PG",
                'P', Items.PAPER,
                'L', Tags.Items.GEMS_LAPIS,
                'G', Tags.Items.NUGGETS_GOLD
        ).build(consumer);

        shapeless(ModItems.AUGMENT_CORE.get(), 4, ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), ModItems.BLANK_UPGRADE.get()
        ).build(consumer);

        // augments
        shaped(ModItems.REGULATOR_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                " Q /CMC/ Q ",
                'Q', Tags.Items.GEMS_QUARTZ,
                'M', ModItems.AUGMENT_CORE.get(),
                'C', Items.COMPARATOR
        ).build(consumer);

        shapeless(ModItems.MIMIC_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Tags.Items.OBSIDIAN, Tags.Items.DUSTS_REDSTONE, Tags.Items.DUSTS_GLOWSTONE
        ).build(consumer);

        shaped(ModItems.REDSTONE_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                " T /RMR/ T ",
                'M', ModItems.AUGMENT_CORE.get(),
                'R', Tags.Items.DUSTS_REDSTONE,
                'T', Items.REDSTONE_TORCH
        ).build(consumer);

        shapeless(ModItems.XP_VACUUM_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Items.SOUL_SAND
        ).build(consumer);

        shaped(ModItems.RANGE_DOWN_AUGMENT.get(), 4, ModItems.AUGMENT_CORE.get(),
                " S /QMQ/ Q ",
                'M', ModItems.AUGMENT_CORE.get(),
                'Q', Tags.Items.GEMS_QUARTZ,
                'S', Tags.Items.RODS_WOODEN
        ).build(consumer);

        shapeless(ModItems.RANGE_DOWN_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.RANGE_UP_AUGMENT.get()
        ).build(consumer, RL("range_down_from_up"));

        shaped(ModItems.RANGE_UP_AUGMENT.get(), 4, ModItems.AUGMENT_CORE.get(),
                " Q /QMQ/ S ",
                'S', Tags.Items.RODS_WOODEN,
                'Q', Tags.Items.GEMS_QUARTZ,
                'M', ModItems.AUGMENT_CORE.get()
        ).build(consumer);

        shapeless(ModItems.RANGE_UP_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.RANGE_DOWN_AUGMENT.get()
        ).build(consumer, RL("range_up_from_down"));

        shaped(ModItems.PUSHING_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                "PMP",
                'P', Items.PISTON,
                'M', ModItems.AUGMENT_CORE.get()
        ).build(consumer);

        shapeless(ModItems.PICKUP_DELAY_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Tags.Items.SLIMEBALLS
        ).build(consumer);

        shapeless(ModItems.FAST_PICKUP_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), Items.FISHING_ROD
        ).build(consumer);

        shapeless(ModItems.STACK_AUGMENT.get(), ModItems.AUGMENT_CORE.get(),
                ModItems.AUGMENT_CORE.get(), ModItems.STACK_UPGRADE.get()
        ).build(consumer);

        // modules
        shapeless(ModItems.EXTRUDER_MODULE_1.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), ModItems.PLACER_MODULE.get(), ModItems.BREAKER_MODULE.get()
        ).build(consumer);

        shaped(ModItems.FLUID_MODULE.get(), ModItems.BLANK_MODULE.get(),
                " C /GMG",
                'G', Tags.Items.GLASS,
                'C', Items.CAULDRON,
                'M', ModItems.BLANK_MODULE.get()
        ).build(consumer);

        shapeless(ModItems.FLUID_MODULE_2.get(), ModItems.FLUID_MODULE.get(),
                ModItems.FLUID_MODULE.get(), Items.PRISMARINE_SHARD
        ).build(consumer);

        shapeless(ModItems.FLUID_MODULE_2.get(), 4, ModItems.FLUID_MODULE.get(),
                ModItems.FLUID_MODULE.get(), ModItems.FLUID_MODULE.get(),
                ModItems.FLUID_MODULE.get(), ModItems.FLUID_MODULE.get(),
                Items.PRISMARINE_SHARD
        ).build(consumer, RL("fluid_module_2_x4"));

        shapeless(ModItems.SENDER_MODULE_1.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.BOW, Tags.Items.ARROWS
        ).build(consumer);

        shapeless(ModItems.SENDER_MODULE_3.get(), ModItems.BLANK_MODULE.get(),
                ModItems.SENDER_MODULE_2.get(), Items.END_STONE, Items.ENDER_CHEST
        ).build(consumer);

        shaped(ModItems.DISTRIBUTOR_MODULE.get(), ModItems.BLANK_MODULE.get(),
                " S /SMS",
                'M', ModItems.BLANK_MODULE.get(),
                'S', ModItems.SENDER_MODULE_2.get()
        ).build(consumer);

        shapeless(ModItems.SENDER_MODULE_2.get(), ModItems.BLANK_MODULE.get(),
                ModItems.SENDER_MODULE_1.get(), Items.ENDER_PEARL
        ).build(consumer);

        shapeless(ModItems.BREAKER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.IRON_PICKAXE
        ).build(consumer);

        shapeless(ModItems.DROPPER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.DROPPER
        ).build(consumer);

        shapeless(ModItems.EXTRUDER_MODULE_2.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Tags.Items.CHESTS_WOODEN
        ).build(consumer);

        shapeless(ModItems.FLINGER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.DROPPER_MODULE.get(), Items.GUNPOWDER
        ).build(consumer);

        shapeless(ModItems.DETECTOR_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.COMPARATOR
        ).build(consumer);

        shapeless(ModItems.PLACER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.DISPENSER, Items.DIRT
        ).build(consumer);

        shaped(ModItems.PLAYER_MODULE.get(), ModItems.BLANK_MODULE.get(),
                " H /SZP/ C ",
                'C', Items.DIAMOND_CHESTPLATE,
                'P', ModItems.PULLER_MODULE_2.get(),
                'Z', Items.WITHER_SKELETON_SKULL,
                'H', Items.DIAMOND_HELMET,
                'S', ModItems.SENDER_MODULE_3.get()
        ).build(consumer);

        shapeless(ModItems.PULLER_MODULE_1.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.STICKY_PISTON
        ).build(consumer);

        shapeless(ModItems.SENDER_MODULE_2.get(), 4, ModItems.BLANK_MODULE.get(),
                ModItems.SENDER_MODULE_1.get(),
                ModItems.SENDER_MODULE_1.get(),
                ModItems.SENDER_MODULE_1.get(),
                ModItems.SENDER_MODULE_1.get(),
                Items.ENDER_PEARL
        ).build(consumer, RL("sender_module_2_x4"));

        shapeless(ModItems.PULLER_MODULE_2.get(), 4, ModItems.BLANK_MODULE.get(),
                ModItems.PULLER_MODULE_1.get(), ModItems.PULLER_MODULE_1.get(), ModItems.PULLER_MODULE_1.get(), ModItems.PULLER_MODULE_1.get(), Items.ENDER_PEARL
        ).build(consumer, RL("puller_module_2_x4"));

        shapeless(ModItems.VACUUM_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.HOPPER, Items.ENDER_EYE
        ).build(consumer);

        shaped(ModItems.ACTIVATOR_MODULE.get(), ModItems.BLANK_MODULE.get(),
                "RLR/DMD/RQR",
                'Q', Tags.Items.GEMS_QUARTZ,
                'D', Items.DISPENSER,
                'R', Tags.Items.DUSTS_REDSTONE,
                'M', ModItems.BLANK_MODULE.get(),
                'L', Items.LEVER
        ).build(consumer);

        shapeless(ModItems.PULLER_MODULE_2.get(), ModItems.BLANK_MODULE.get(),
                ModItems.PULLER_MODULE_1.get(), Items.ENDER_PEARL
        ).build(consumer);

        shapeless(ModItems.VOID_MODULE.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.LAVA_BUCKET
        ).build(consumer);

        shapeless(ModItems.SENDER_MODULE_1.get(), ModItems.BLANK_MODULE.get(),
                ModItems.BLANK_MODULE.get(), Items.PISTON
        ).build(consumer, RL("sender_module_1_alt"));

        // upgrades
        shaped(ModItems.MUFFLER_UPGRADE.get(), 4, ModItems.BLANK_UPGRADE.get(),
                " W /WBW/ W ",
                'W', ItemTags.WOOL,
                'B', ModItems.BLANK_UPGRADE.get()
        ).build(consumer);

        shaped(ModItems.SECURITY_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                " Q /NBN/ R ",
                'B', ModItems.BLANK_UPGRADE.get(),
                'N', Tags.Items.NUGGETS_GOLD,
                'R', Tags.Items.DUSTS_REDSTONE,
                'Q', Tags.Items.GEMS_QUARTZ
        ).build(consumer);

        shaped(ModItems.SPEED_UPGRADE.get(), 3, ModItems.BLANK_UPGRADE.get(),
                "RIR/NBN/GZG",
                'I', Tags.Items.INGOTS_GOLD,
                'G', Tags.Items.GUNPOWDER,
                'R', Tags.Items.DUSTS_REDSTONE,
                'B', ModItems.BLANK_UPGRADE.get(),
                'N', Tags.Items.NUGGETS_GOLD,
                'Z', Tags.Items.RODS_BLAZE
        ).build(consumer);

        shaped(ModItems.BLAST_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                "IOI/OBO/IOI",
                'O', Tags.Items.OBSIDIAN,
                'I', Items.IRON_BARS,
                'B', ModItems.BLANK_UPGRADE.get()
        ).build(consumer);

        shapeless(ModItems.CAMOUFLAGE_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                ModItems.BLANK_UPGRADE.get(), Tags.Items.DYES_RED, Tags.Items.DYES_GREEN, Tags.Items.DYES_BLUE
        ).build(consumer);

        shaped(ModItems.FLUID_UPGRADE.get(), 3, ModItems.BLANK_UPGRADE.get(),
                " U /GBG",
                'G', Tags.Items.GLASS,
                'U', Items.BUCKET,
                'B', ModItems.BLANK_UPGRADE.get()
        ).build(consumer);

        shaped(ModItems.SYNC_UPGRADE.get(), 16, ModItems.BLANK_UPGRADE.get(),
                "RST/RBR",
                'S', Tags.Items.STONE,
                'T', Items.REDSTONE_TORCH,
                'B', ModItems.BLANK_UPGRADE.get(),
                'R', Tags.Items.DUSTS_REDSTONE
        ).build(consumer);

        shapeless(ModItems.STACK_UPGRADE.get(), ModItems.BLANK_UPGRADE.get(),
                ModItems.BLANK_UPGRADE.get(), ItemTags.STONE_BRICKS, Tags.Items.INGOTS_BRICK
        ).build(consumer);

        // filters
        shaped(ModItems.BULK_ITEM_FILTER.get(), ModItems.BLANK_MODULE.get(),
                "IGI/MDM/IGI",
                'G', Tags.Items.GLASS,
                'D', Tags.Items.GEMS_DIAMOND,
                'I', Tags.Items.INGOTS_IRON,
                'M', ModItems.BLANK_MODULE.get()
        ).build(consumer);

        shapeless(ModItems.MOD_FILTER.get(), ModItems.BULK_ITEM_FILTER.get(),
                ModItems.BULK_ITEM_FILTER.get(), Items.REPEATER, Items.REDSTONE_TORCH
        ).build(consumer);

        shaped(ModItems.INSPECTION_FILTER.get(), ModItems.BULK_ITEM_FILTER.get(),
                "EBE/ P ",
                'B', ModItems.BULK_ITEM_FILTER.get(),
                'P', Items.PAPER,
                'E', Items.SPIDER_EYE
        ).build(consumer);

        shapeless(ModItems.REGEX_FILTER.get(), ModItems.BULK_ITEM_FILTER.get(),
                ModItems.BULK_ITEM_FILTER.get(), Items.COMPARATOR
        ).build(consumer);

        CustomRecipeBuilder.customRecipe(ModRecipes.MODULE_ENCHANT.get()).build(consumer, RL("enchant_module").toString());
        CustomRecipeBuilder.customRecipe(ModRecipes.MODULE_RESET.get()).build(consumer, RL("reset_module").toString());
        CustomRecipeBuilder.customRecipe(ModRecipes.GUIDE_BOOK.get()).build(consumer, RL("guide_book").toString());
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder shaped(T result, T required, String pattern, Object... keys) {
        return shaped(result, 1, required, pattern, keys);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapedRecipeBuilder shaped(T result, int count, T required, String pattern, Object... keys) {
        ShapedRecipeBuilder b = ShapedRecipeBuilder.shapedRecipe(result, count);
        Arrays.stream(pattern.split("/")).forEach(b::patternLine);
        for (int i = 0; i < keys.length; i += 2) {
            Object v = keys[i + 1];
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.key((Character) keys[i], (Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.key((Character) keys[i], (IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.key((Character) keys[i], (Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder shapeless(T result, T required, Object... ingredients) {
        return shapeless(result, 1, required, ingredients);
    }

    private <T extends IItemProvider & IForgeRegistryEntry<?>> ShapelessRecipeBuilder shapeless(T result, int count, T required, Object... ingredients) {
        ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapelessRecipe(result, count);
        for (Object v : ingredients) {
            if (v instanceof Tag<?>) {
                //noinspection unchecked
                b.addIngredient((Tag<Item>) v);
            } else if (v instanceof IItemProvider) {
                b.addIngredient((IItemProvider) v);
            } else if (v instanceof Ingredient) {
                b.addIngredient((Ingredient) v);
            } else {
                throw new IllegalArgumentException("bad type for recipe ingredient " + v);
            }
        }
        b.addCriterion("has_" + safeName(required), this.hasItem(required));
        return b;
    }

    private String safeName(IForgeRegistryEntry<?>  i) {
        return safeName(i.getRegistryName());
    }

    private String safeName(ResourceLocation registryName) {
        return registryName.getPath().replace('/', '_');
    }

    @Override
    public String getName() {
        return "Modular Routers Recipes";
    }
}
