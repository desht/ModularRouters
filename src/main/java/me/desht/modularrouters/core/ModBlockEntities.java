package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.block.tile.TemplateFrameBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModularRouters.MODID);

    public static final Supplier<BlockEntityType<ModularRouterBlockEntity>> MODULAR_ROUTER
            = register("modular_router", () -> new BlockEntityType<>(ModularRouterBlockEntity::new, ModBlocks.MODULAR_ROUTER.get()));
    public static final Supplier<BlockEntityType<TemplateFrameBlockEntity>> TEMPLATE_FRAME
            = register("template_frame", () -> new BlockEntityType<>(TemplateFrameBlockEntity::new, ModBlocks.TEMPLATE_FRAME.get()));

    private static <T extends BlockEntityType<?>> Supplier<T> register(String name, Supplier<T> sup) {
        return BLOCK_ENTITIES.register(name, sup);
    }
}
