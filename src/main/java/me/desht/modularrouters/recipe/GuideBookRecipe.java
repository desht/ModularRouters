package me.desht.modularrouters.recipe;

import com.mojang.serialization.MapCodec;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.integration.patchouli.PatchouliHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.util.List;

public class GuideBookRecipe extends CustomRecipe {
    public static final GuideBookRecipe INSTANCE = new GuideBookRecipe();
    public static final MapCodec<GuideBookRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, GuideBookRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<GuideBookRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private static final List<Ingredient> INGREDIENTS = List.of(
            Ingredient.of(Items.BOOK),
            Ingredient.of(ModItems.BLANK_MODULE.get())
    );

    public static ItemStack makeGuideBook() {
        return ModList.get().isLoaded("patchouli") ?
                PatchouliHelper.makePatchouliBook() :
                Util.make(new ItemStack(Items.BOOK),
                        s -> s.set(DataComponents.CUSTOM_NAME, Component.literal("Patchouli is not installed")));
    }

    @Override
    public boolean matches(CraftingInput pInput, Level pLevel) {
        return ModCraftingHelper.allPresent(pInput, INGREDIENTS);
    }

    @Override
    public ItemStack assemble(CraftingInput pInput) {
        return makeGuideBook();
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.GUIDE_BOOK.get();
    }
}
