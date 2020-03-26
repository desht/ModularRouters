package me.desht.modularrouters.recipe;

import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class GuideBookRecipe extends SpecialRecipe {
    @ObjectHolder("patchouli:guide_book")
    private static Item GUIDE_BOOK = null;

    private static final String NBT_KEY = "patchouli:book";
    private static final String NBT_VAL = "modularrouters:book";

    public GuideBookRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        if (GUIDE_BOOK == null) return false;

        boolean bookFound = false, moduleFound = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            Item item = inv.getStackInSlot(i).getItem();
            if (item == ModItems.BLANK_MODULE.get()) {
                if (moduleFound) return false;
                moduleFound = true;
            } else if (item == Items.BOOK) {
                if (bookFound) return false;
                bookFound = true;
            } else if (item != Items.AIR) {
                return false;
            }
        }
        return bookFound && moduleFound;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        if (GUIDE_BOOK == null) return ItemStack.EMPTY;

        ItemStack guideBook = new ItemStack(GUIDE_BOOK);
        CompoundNBT tag = guideBook.getOrCreateTag();
        tag.putString(NBT_KEY, NBT_VAL);
        return guideBook;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.GUIDE_BOOK.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.from(Ingredient.EMPTY, Ingredient.fromItems(ModItems.BLANK_MODULE.get()), Ingredient.fromItems(Items.BOOK));
    }
}
