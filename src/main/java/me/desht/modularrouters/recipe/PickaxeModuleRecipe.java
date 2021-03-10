package me.desht.modularrouters.recipe;

import com.google.common.collect.ImmutableList;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.IPickaxeUser;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.crafting.StackList;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * For modules which need a pickaxe in their recipe to set their harvest level.
 */
public abstract class PickaxeModuleRecipe extends ShapelessRecipe {
    PickaxeModuleRecipe(ResourceLocation resourceLocation, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(resourceLocation, "", result, ingredients);

        Validate.isTrue(result.getItem() instanceof IPickaxeUser,
                "recipe " + resourceLocation.toString() + ": result is not a IPickaxeUser!");
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        if (!super.matches(inv, worldIn)) return false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem().getToolTypes(stack).contains(ToolType.PICKAXE) || stack.getItem() instanceof IPickaxeUser) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack pick = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem().getToolTypes(stack).contains(ToolType.PICKAXE)) {
                pick = stack;
                break;
            } else if (stack.getItem() instanceof IPickaxeUser) {
                pick = ((IPickaxeUser) stack.getItem()).getPickaxe(stack);
                break;
            }
        }
        if (!pick.isEmpty()) {
            ItemStack result = super.assemble(inv);
            return ((IPickaxeUser) result.getItem()).setPickaxe(result, pick);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static class BreakerModuleRecipe extends PickaxeModuleRecipe {
        public BreakerModuleRecipe(ResourceLocation resourceLocation) {
            super(resourceLocation, new ItemStack(ModItems.BREAKER_MODULE.get()), ingredients());
        }

        private static NonNullList<Ingredient> ingredients() {
            return NonNullList.of(Ingredient.EMPTY,
                    Ingredient.of(ModItems.BLANK_MODULE.get()),
                    new PickaxeIngredient()
            );
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return ModRecipes.BREAKER_MODULE.get();
        }
    }

    public static class ExtruderModule1Recipe extends PickaxeModuleRecipe {
        public ExtruderModule1Recipe(ResourceLocation resourceLocation) {
            super(resourceLocation, new ItemStack(ModItems.EXTRUDER_MODULE_1.get()), ingredients());
        }

        private static NonNullList<Ingredient> ingredients() {
            return NonNullList.of(Ingredient.EMPTY,
                    Ingredient.of(ModItems.BLANK_MODULE.get()),
                    Ingredient.of(ModItems.PLACER_MODULE.get()),
                    Ingredient.of(ModItems.BREAKER_MODULE.get())
            );
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return ModRecipes.EXTRUDER_MODULE_1.get();
        }
    }

    private static class PickaxeIngredient extends Ingredient {
        PickaxeIngredient() {
            // this is for the benefit of getMatchingStacks()
            super(Stream.of(new StackList(ImmutableList.of(
                    new ItemStack(Items.WOODEN_PICKAXE),
                    new ItemStack(Items.STONE_PICKAXE),
                    new ItemStack(Items.IRON_PICKAXE),
                    new ItemStack(Items.DIAMOND_PICKAXE),
                    new ItemStack(Items.NETHERITE_PICKAXE)
            ))));
        }

        @Override
        public boolean test(@Nullable ItemStack stack) {
            // should match anything that claims to be a pickaxe
            return stack != null && stack.getItem().getToolTypes(stack).contains(ToolType.PICKAXE) && stack.getDamageValue() == 0;
        }
    }
}
