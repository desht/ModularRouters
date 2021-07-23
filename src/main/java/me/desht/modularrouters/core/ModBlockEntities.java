package me.desht.modularrouters.core;

import com.google.common.collect.ImmutableSet;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.block.tile.TemplateFrameBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ModularRouters.MODID);

    public static final RegistryObject<BlockEntityType<ModularRouterBlockEntity>> ITEM_ROUTER
            = register("item_router", () -> new BlockEntityType<>(ModularRouterBlockEntity::new, ImmutableSet.of(ModBlocks.ITEM_ROUTER.get()), null));
    public static final RegistryObject<BlockEntityType<TemplateFrameBlockEntity>> TEMPLATE_FRAME
            = register("template_frame", () -> new BlockEntityType<>(TemplateFrameBlockEntity::new, ImmutableSet.of(ModBlocks.TEMPLATE_FRAME.get()), null));

    private static <T extends BlockEntityType<?>> RegistryObject<T> register(String name, Supplier<T> sup) {
        return TILE_ENTITIES.register(name, sup);
    }
}
