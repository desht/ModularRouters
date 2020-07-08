package me.desht.modularrouters.recipe;

import com.google.gson.JsonObject;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static me.desht.modularrouters.util.MiscUtil.RL;

@SuppressWarnings("unused")
public class Extruder1Factory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        return new Extruder1Factory.Extruder1Recipe(RL("extruder1"), recipe.getRecipeOutput(), recipe.getIngredients().toArray());
    }

    private class Extruder1Recipe extends ShapelessOreRecipe {
        Extruder1Recipe(ResourceLocation id, ItemStack result, Object... recipe) {
            super(id, result, recipe);
        }

        @Override
        public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
            ItemStack[] ing = findIngredients(inv);
            return ing != null && !ing[0].isEmpty() && !ing[1].isEmpty() && !ing[2].isEmpty();
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
            ItemStack[] ing = findIngredients(inv);
            if (ing == null) return ItemStack.EMPTY;

            ItemStack res = ModuleHelper.makeItemStack(ItemModule.ModuleType.EXTRUDER);
            ItemStack pick = ModuleHelper.getPickaxe(ing[1]);
            ModuleHelper.setPickaxe(res, pick);

            return res;
        }

        // order: blank, breaker, placer
        private ItemStack[] findIngredients(InventoryCrafting inv) {
            ItemStack[] res = new ItemStack[3];
            Arrays.fill(res, ItemStack.EMPTY);

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() == RegistrarMR.BLANK_MODULE) {
                    if (!res[0].isEmpty()) return null;
                    res[0] = stack;
                } else if (stack.getItem() == RegistrarMR.MODULE && stack.getMetadata() == ItemModule.ModuleType.BREAKER.ordinal()) {
                    if (!res[1].isEmpty()) return null;
                    res[1] = stack;
                } else if (stack.getItem() == RegistrarMR.MODULE && stack.getMetadata() == ItemModule.ModuleType.PLACER.ordinal()) {
                    if (!res[2].isEmpty()) return null;
                    res[2] = stack;
                } else if (!stack.isEmpty()) {
                    return null;
                }
            }

            return res;
        }
    }
}
