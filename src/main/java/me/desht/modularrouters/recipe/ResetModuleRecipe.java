package me.desht.modularrouters.recipe;

import com.mojang.serialization.MapCodec;
import me.desht.modularrouters.core.ModRecipes;
import me.desht.modularrouters.item.module.IPickaxeUser;
import me.desht.modularrouters.item.module.ModuleItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ResetModuleRecipe extends CustomRecipe {
    public static final ResetModuleRecipe INSTANCE = new ResetModuleRecipe();
    public static final MapCodec<ResetModuleRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetModuleRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<ResetModuleRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput inv, Level wrldIn) {
        ModuleItem module = null;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (module != null || !(stack.getItem() instanceof ModuleItem)) {
                    return false;
                }
                module = (ModuleItem) stack.getItem();
            }
        }
        return module != null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack moduleStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ModuleItem) {
                moduleStack = stack;
                break;
            }
        }

        if (!moduleStack.isEmpty()) {
            ItemStack newStack = new ItemStack(moduleStack.getItem());
            ItemStack pick = IPickaxeUser.getPickaxe(moduleStack);
            if (!pick.isEmpty()) {
                IPickaxeUser.setPickaxe(newStack, pick);
            }
            return newStack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.MODULE_RESET.get();
    }
}
