package me.desht.modularrouters;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class ModularRoutersTags {
    public static class Items {
        public static final Tag<Item> MODULES = modTag("modules");
        public static final Tag<Item> UPGRADES = modTag("upgrades");
        public static final Tag<Item> AUGMENTS = modTag("augments");
        public static final Tag<Item> FILTERS = modTag("filters");

        static Tag<Item> tag(String modid, String name) {
            return ModularRoutersTags.tag(ItemTags.Wrapper::new, modid, name);
        }

        static Tag<Item> modTag(String name) {
            return tag(ModularRouters.MODID, name);
        }

        static Tag<Item> forgeTag(String name) {
            return tag("forge", name);
        }
    }

    static <T extends Tag<?>> T tag(Function<ResourceLocation, T> creator, String modid, String name) {
        return creator.apply(new ResourceLocation(modid, name));
    }

    static <T extends Tag<?>> T modTag(Function<ResourceLocation, T> creator, String name) {
        return tag(creator, ModularRouters.MODID, name);
    }

    static <T extends Tag<?>> T forgeTag(Function<ResourceLocation, T> creator, String name) {
        return tag(creator, "forge", name);
    }
}
