package me.desht.modularrouters;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModularRoutersTags {
    public static class Items {
        public static final TagKey<Item> MODULES = modTag("modules");
        public static final TagKey<Item> UPGRADES = modTag("upgrades");
        public static final TagKey<Item> AUGMENTS = modTag("augments");
        public static final TagKey<Item> FILTERS = modTag("filters");
        public static final TagKey<Item> ACTIVATOR_BLACKLIST = modTag("activator_blacklist");

        private static TagKey<Item> modTag(String name) {
            return TagKey.create(Registries.ITEM, MiscUtil.RL(name));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> CAMO_BLACKLIST = modTag("camo_blacklist");

        private static TagKey<Block> modTag(String name) {
            return TagKey.create(Registries.BLOCK, MiscUtil.RL(name));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> activatorInteractBlacklist = modTag("activator_interact_blacklist");
        public static final TagKey<EntityType<?>> activatorAttackBlacklist = modTag("activator_attack_blacklist");

        private static TagKey<EntityType<?>> modTag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, MiscUtil.RL(name));
        }
    }
}
