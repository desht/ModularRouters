package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.registries.ForgeRegistries;

public class GuideBookRecipe extends ShapelessRecipe {
    private static final ResourceLocation BOOK_ID = new ResourceLocation("patchouli:guide_book");

    private static final String NBT_KEY = "patchouli:book";
    private static final String NBT_VAL = "modularrouters:book";

    public GuideBookRecipe(CraftingBookCategory category) {
        super( "modularrouters:guide_book", category, makeGuideBook(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.BOOK), Ingredient.of(ModItems.BLANK_MODULE.get()))
        );
    }

    public static ItemStack makeGuideBook() {
        Item bookItem = ForgeRegistries.ITEMS.getValue(BOOK_ID);
        if (bookItem == null) {
            ItemStack stack = new ItemStack(Items.PAPER);
            stack.setHoverName(Component.literal("Install Patchouli for the guide book!"));
            return stack;
        }
        ItemStack book = new ItemStack(bookItem);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString(NBT_KEY, NBT_VAL);
        return book;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GUIDE_BOOK.get();
    }
}
