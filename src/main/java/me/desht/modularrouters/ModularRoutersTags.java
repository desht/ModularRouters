package me.desht.modularrouters;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

public class ModularRoutersTags {
    public static class Items {
        public static final Tag.Named<Item> MODULES = modTag("modules");
        public static final Tag.Named<Item> UPGRADES = modTag("upgrades");
        public static final Tag.Named<Item> AUGMENTS = modTag("augments");
        public static final Tag.Named<Item> FILTERS = modTag("filters");

        private static Tag.Named<Item> modTag(String name) {
            return ItemTags.bind(new ResourceLocation(ModularRouters.MODID, name).toString());
        }

        private static Tag.Named<Item> forgeTag(String name) {
            return ItemTags.bind(new ResourceLocation("forge", name).toString());
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
