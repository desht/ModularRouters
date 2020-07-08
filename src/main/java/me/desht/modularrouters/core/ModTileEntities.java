package me.desht.modularrouters.core;

import com.google.common.collect.ImmutableSet;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ModularRouters.MODID);

    public static final RegistryObject<TileEntityType<TileEntityItemRouter>> ITEM_ROUTER
            = register("item_router", () -> new TileEntityType<>(TileEntityItemRouter::new, ImmutableSet.of(ModBlocks.ITEM_ROUTER.get()), null));
    public static final RegistryObject<TileEntityType<TileEntityTemplateFrame>> TEMPLATE_FRAME
            = register("template_frame", () -> new TileEntityType<>(TileEntityTemplateFrame::new, ImmutableSet.of(ModBlocks.TEMPLATE_FRAME.get()), null));

    private static <T extends TileEntityType<?>> RegistryObject<T> register(String name, Supplier<T> sup) {
        return TILE_ENTITIES.register(name, sup);
    }
}
