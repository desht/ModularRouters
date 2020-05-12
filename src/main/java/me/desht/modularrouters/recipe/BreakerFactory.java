package me.desht.modularrouters.recipe;

import com.google.gson.JsonObject;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static me.desht.modularrouters.util.MiscUtil.RL;

@SuppressWarnings("unused")
public class BreakerFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        return new BreakerFactory.BreakerRecipe(RL("breaker"), recipe.getRecipeOutput(), recipe.getIngredients().toArray());
    }

    private class BreakerRecipe extends ShapelessOreRecipe {
        BreakerRecipe(ResourceLocation id, ItemStack result, Object... recipe) {
            super(id, result, recipe);
        }

        @Override
        public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
            ItemStack[] ing = findIngredients(inv);
            return ing != null && !ing[0].isEmpty() && !ing[1].isEmpty();
        }

        @Nonnull
        @Override
        public NonNullList<Ingredient> getIngredients() {
            return NonNullList.from(Ingredient.EMPTY, Ingredient.fromItem(RegistrarMR.BLANK_MODULE),
                    Ingredient.fromItems(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE));
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
            ItemStack[] ing = findIngredients(inv);
            if (ing == null) return ItemStack.EMPTY;

            ItemStack output = ModuleHelper.makeItemStack(ItemModule.ModuleType.BREAKER);
            ModuleHelper.setPickaxe(output, ing[1]);
            return output;
        }

        // order: blank, pickaxe
        private ItemStack[] findIngredients(InventoryCrafting inv) {
            ItemStack[] res = new ItemStack[2];
            Arrays.fill(res, ItemStack.EMPTY);

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() == RegistrarMR.BLANK_MODULE) {
                    if (!res[0].isEmpty()) return null;
                    res[0] = stack;
                } else if (stack.getItem().getToolClasses(inv.getStackInSlot(i)).contains("pickaxe")) {
                    if (!res[1].isEmpty()) return null;
                    res[1] = stack;
                } else if (!stack.isEmpty()) {
                    return null;
                }
            }

            return res;
        }
    }
}
