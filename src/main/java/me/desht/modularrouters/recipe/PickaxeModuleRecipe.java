package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.IPickaxeUser;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ToolActions;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * For modules which need a pickaxe in their recipe to set their harvest level.
 */
public abstract class PickaxeModuleRecipe extends ShapelessRecipe {
    PickaxeModuleRecipe(String name, ItemStack result, NonNullList<Ingredient> ingredients, CraftingBookCategory category) {
        super(name, category, result, ingredients);

        Validate.isTrue(result.getItem() instanceof IPickaxeUser,
                "recipe " + name + ": result is not a IPickaxeUser!");
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        if (!super.matches(inv, worldIn)) return false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isValidPickaxe(stack) || stack.getItem() instanceof IPickaxeUser) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack pick = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isValidPickaxe(stack)) {
                pick = stack;
                break;
            } else if (stack.getItem() instanceof IPickaxeUser pickaxeUser) {
                pick = pickaxeUser.getPickaxe(stack);
                break;
            }
        }
        if (!pick.isEmpty()) {
            ItemStack result = super.assemble(inv, registryAccess);
            return ((IPickaxeUser) result.getItem()).setPickaxe(result, pick);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static class BreakerModuleRecipe extends PickaxeModuleRecipe {
        public BreakerModuleRecipe(CraftingBookCategory category) {
            super("modularrouters:breaker", new ItemStack(ModItems.BREAKER_MODULE.get()), ingredients(), category);
        }

        private static NonNullList<Ingredient> ingredients() {
            return NonNullList.of(Ingredient.EMPTY,
                    Ingredient.of(ModItems.BLANK_MODULE.get()),
                    new PickaxeIngredient()
            );
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return ModRecipes.BREAKER_MODULE.get();
        }
    }

    public static class ExtruderModule1Recipe extends PickaxeModuleRecipe {
        public ExtruderModule1Recipe(CraftingBookCategory category) {
            super("modularrouters:extruder1", new ItemStack(ModItems.EXTRUDER_MODULE_1.get()), ingredients(), category);
        }

        private static NonNullList<Ingredient> ingredients() {
            return NonNullList.of(Ingredient.EMPTY,
                    Ingredient.of(ModItems.BLANK_MODULE.get()),
                    Ingredient.of(ModItems.PLACER_MODULE.get()),
                    Ingredient.of(ModItems.BREAKER_MODULE.get())
            );
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return ModRecipes.EXTRUDER_MODULE_1.get();
        }
    }

    private static class PickaxeIngredient extends Ingredient {
        PickaxeIngredient() {
            // this is for the benefit of getMatchingStacks()
            super(Stream.of(new TagValue(ItemTags.PICKAXES)));

//                    Tags.Items.ImmutableList.of(
//                    new ItemStack(Items.WOODEN_PICKAXE),
//                    new ItemStack(Items.STONE_PICKAXE),
//                    new ItemStack(Items.IRON_PICKAXE),
//                    new ItemStack(Items.DIAMOND_PICKAXE),
//                    new ItemStack(Items.NETHERITE_PICKAXE)
//            ))));
        }

        @Override
        public boolean test(@Nullable ItemStack stack) {
            return isValidPickaxe(stack);
        }
    }

    private static boolean isValidPickaxe(ItemStack stack) {
        return stack != null && stack.getItem().canPerformAction(stack, ToolActions.PICKAXE_DIG) && stack.getDamageValue() == 0;
    }
}
