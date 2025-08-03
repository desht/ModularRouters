package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.IPickaxeUser;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.function.Predicate;

/**
 * For modules which need a pickaxe in their recipe to set their harvest level.
 */
public abstract class PickaxeModuleRecipe extends CustomRecipe {
    private final ItemStack result;

    PickaxeModuleRecipe(String name, ItemStack result, CraftingBookCategory category) {
        super(category);

        this.result = result;

        Validate.isTrue(result.getItem() instanceof IPickaxeUser,
                "recipe " + name + ": result is not a IPickaxeUser!");
    }

    protected abstract List<? extends Predicate<ItemStack>> ingredients();

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
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
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
            return IPickaxeUser.setPickaxe(result.copy(), pick);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static class BreakerModuleRecipe extends PickaxeModuleRecipe {
        private static final List<Predicate<ItemStack>> PREDICATES = List.of(
                Ingredient.of(ModItems.BLANK_MODULE.get()),
                stack -> stack.is(ItemTags.PICKAXES)
        );

        public BreakerModuleRecipe(CraftingBookCategory category) {
            super("modularrouters:breaker", ModItems.BREAKER_MODULE.toStack(), category);
        }

        @Override
        protected List<? extends Predicate<ItemStack>> ingredients() {
            return PREDICATES;
        }

        @Override
        public RecipeSerializer<? extends CustomRecipe> getSerializer() {
            return ModRecipes.BREAKER_MODULE.get();
        }
    }

    public static class ExtruderModule1Recipe extends PickaxeModuleRecipe {
        private static final List<Ingredient> INGREDIENTS = List.of(
                Ingredient.of(ModItems.BLANK_MODULE.get()),
                Ingredient.of(ModItems.PLACER_MODULE.get()),
                Ingredient.of(ModItems.BREAKER_MODULE.get())
        );

        public ExtruderModule1Recipe(CraftingBookCategory category) {
            super("modularrouters:extruder1", ModItems.EXTRUDER_MODULE_1.toStack(), category);
        }

        @Override
        protected List<? extends Predicate<ItemStack>> ingredients() {
            return INGREDIENTS;
        }

        @Override
        public RecipeSerializer<? extends CustomRecipe> getSerializer() {
            return ModRecipes.EXTRUDER_MODULE_1.get();
        }
    }

    private static boolean isValidPickaxe(ItemStack stack) {
//        return stack != null && stack.getItem().canPerformAction(stack, ItemAbilities.PICKAXE_DIG) && stack.getDamageValue() == 0;
        // TODO is this best way of identifying a pickaxe now?
        return stack != null && stack.is(ItemTags.PICKAXES) && stack.getDamageValue() == 0;
    }
}
