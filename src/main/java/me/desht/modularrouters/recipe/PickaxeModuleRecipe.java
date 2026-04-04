package me.desht.modularrouters.recipe;

import com.mojang.serialization.MapCodec;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.IPickaxeUser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * For modules which need a pickaxe in their recipe to set their harvest level.
 */
public abstract class PickaxeModuleRecipe extends CustomRecipe {
    private final ItemStackTemplate result;

    protected PickaxeModuleRecipe(String name, ItemStackTemplate result) {
        Validate.isTrue(result.item().value() instanceof IPickaxeUser,
                "recipe " + name + ": result is not a IPickaxeUser!");

        this.result = result;
    }

    protected abstract List<Ingredient> ingredients();

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        if (!ModCraftingHelper.allPresent(inv, ingredients())) {
            return false;
        }

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isValidPickaxe(stack) || stack.getItem() instanceof IPickaxeUser) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack pick = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isValidPickaxe(stack)) {
                pick = stack;
                break;
            } else if (stack.getItem() instanceof IPickaxeUser) {
                pick = IPickaxeUser.getPickaxe(stack);
                break;
            }
        }
        if (!pick.isEmpty()) {
            return IPickaxeUser.setPickaxe(result.create(), pick);
        } else {
            return ItemStack.EMPTY;
        }
    }

    private static boolean isValidPickaxe(ItemStack stack) {
        // TODO is this best way of identifying a pickaxe now?
        return stack != null && stack.is(ItemTags.PICKAXES) && stack.getDamageValue() == 0;
    }

    public static class BreakerModuleRecipe extends PickaxeModuleRecipe {
        public static final BreakerModuleRecipe INSTANCE = new BreakerModuleRecipe();
        public static final MapCodec<BreakerModuleRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, BreakerModuleRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        public static final RecipeSerializer<BreakerModuleRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

        private static final Lazy<List<Ingredient>> PREDICATES = Lazy.of(() -> List.of(
                Ingredient.of(ModItems.BLANK_MODULE.get()),
                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.PICKAXES))
        ));

        public BreakerModuleRecipe() {
            super("modularrouters:breaker", new ItemStackTemplate(ModItems.BREAKER_MODULE.asItem()));
        }

        @Override
        protected List<Ingredient> ingredients() {
            return PREDICATES.get();
        }

        @Override
        public RecipeSerializer<? extends CustomRecipe> getSerializer() {
            return ModRecipes.BREAKER_MODULE.get();
        }
    }

    public static class ExtruderModule1Recipe extends PickaxeModuleRecipe {
        public static final ExtruderModule1Recipe INSTANCE = new ExtruderModule1Recipe();
        public static final MapCodec<ExtruderModule1Recipe> MAP_CODEC = MapCodec.unit(INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtruderModule1Recipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
        public static final RecipeSerializer<ExtruderModule1Recipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

        private static final List<Ingredient> INGREDIENTS = List.of(
                Ingredient.of(ModItems.BLANK_MODULE.get()),
                Ingredient.of(ModItems.PLACER_MODULE.get()),
                Ingredient.of(ModItems.BREAKER_MODULE.get())
        );

        public ExtruderModule1Recipe() {
            super("modularrouters:extruder1", new ItemStackTemplate(ModItems.EXTRUDER_MODULE_1.get()));
        }

        @Override
        protected List<Ingredient> ingredients() {
            return INGREDIENTS;
        }

        @Override
        public RecipeSerializer<? extends CustomRecipe> getSerializer() {
            return ModRecipes.EXTRUDER_MODULE_1.get();
        }
    }
}
