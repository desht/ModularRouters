package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ObjectHolder;

public class GuideBookRecipe extends ShapelessRecipe {
    @ObjectHolder("patchouli:guide_book")
    private static Item GUIDE_BOOK = null;

    private static final String NBT_KEY = "patchouli:book";
    private static final String NBT_VAL = "modularrouters:book";

    public GuideBookRecipe(ResourceLocation idIn) {
        super(idIn, "", makeGuideBook(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.BOOK), Ingredient.of(ModItems.BLANK_MODULE.get()))
        );
    }

    private static ItemStack makeGuideBook() {
        if (GUIDE_BOOK == null) return ItemStack.EMPTY;
        ItemStack book = new ItemStack(GUIDE_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString(NBT_KEY, NBT_VAL);
        return book;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GUIDE_BOOK.get();
    }
}
