package me.desht.modularrouters;

import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class ModularRoutersTags {
    public static class Items {
        public static final ITag.INamedTag<Item> MODULES = modTag("modules");
        public static final ITag.INamedTag<Item> UPGRADES = modTag("upgrades");
        public static final ITag.INamedTag<Item> AUGMENTS = modTag("augments");
        public static final ITag.INamedTag<Item> FILTERS = modTag("filters");

        private static ITag.INamedTag<Item> modTag(String name) {
            return ItemTags.makeWrapperTag(new ResourceLocation(ModularRouters.MODID, name).toString());
        }

        private static ITag.INamedTag<Item> forgeTag(String name) {
            return ItemTags.makeWrapperTag(new ResourceLocation("forge", name).toString());
        }
    }

//    static <T extends Tag<?>> T tag(Function<ResourceLocation, T> creator, String modid, String name) {
//        return creator.apply(new ResourceLocation(modid, name));
//    }
//
//    static <T extends Tag<?>> T modTag(Function<ResourceLocation, T> creator, String name) {
//        return tag(creator, ModularRouters.MODID, name);
//    }
//
//    static <T extends Tag<?>> T forgeTag(Function<ResourceLocation, T> creator, String name) {
//        return tag(creator, "forge", name);
//    }
}
