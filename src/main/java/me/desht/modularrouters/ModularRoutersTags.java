package me.desht.modularrouters;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class ModularRoutersTags {
    public static class Items {
        public static final TagKey<Item> MODULES = modTag("modules");
        public static final TagKey<Item> UPGRADES = modTag("upgrades");
        public static final TagKey<Item> AUGMENTS = modTag("augments");
        public static final TagKey<Item> FILTERS = modTag("filters");

        private static TagKey<Item> modTag(String name) {
            return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(ModularRouters.MODID, name));
        }

        private static TagKey<Item> forgeTag(String name) {
            return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", name));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> activatorInteractBlacklist = modTag("activator_interact_blacklist");
        public static final TagKey<EntityType<?>> activatorAttackBlacklist = modTag("activator_attack_blacklist");

        private static TagKey<EntityType<?>> modTag(String name) {
            return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(ModularRouters.MODID, name));
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
