package me.desht.modularrouters.integration.patchouli;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PatchouliHelper {
    private static final Identifier BOOK_ID = Identifier.fromNamespaceAndPath(ModularRouters.MODID, "book");

    // TODO: Reimplement once Patchouli is updated
    public static ItemStack makePatchouliBook() {
        return new ItemStack(Items.BOOK);
//        return PatchouliAPI.get().getBookStack(BOOK_ID);
    }
}
