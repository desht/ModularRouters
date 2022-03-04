package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.block.TemplateFrameBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModularRouters.MODID);
    public static final DeferredRegister<Item> ITEMS = ModItems.ITEMS;

    private static final BlockBehaviour.Properties ROUTER_PROPS = Block.Properties.of(Material.METAL)
            .strength(1.5f, 6f)
            .sound(SoundType.METAL)
            .noOcclusion();
    private static final BlockBehaviour.Properties TEMPLATE_FRAME_PROPS = Block.Properties.of(Material.GLASS)
            .isValidSpawn((state, world, pos, entityType) -> false);

    public static final RegistryObject<ModularRouterBlock> MODULAR_ROUTER = register("modular_router",
            () -> new ModularRouterBlock(ROUTER_PROPS));
    public static final RegistryObject<TemplateFrameBlock> TEMPLATE_FRAME = registerNoItem("template_frame",
            () -> new TemplateFrameBlock(TEMPLATE_FRAME_PROPS));

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        return register(name, sup, ModBlocks::itemDefault);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup, Function<RegistryObject<T>, Supplier<? extends Item>> itemCreator) {
        RegistryObject<T> ret = registerNoItem(name, sup);
        ITEMS.register(name, itemCreator.apply(ret));
        return ret;
    }

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<? extends T> sup) {
        return BLOCKS.register(name, sup);
    }

    private static Supplier<BlockItem> itemDefault(final RegistryObject<? extends Block> block) {
        return item(block, ModItems.MR_CREATIVE_TAB);
    }

    private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block, final CreativeModeTab itemGroup) {
        return () -> new BlockItem(block.get(), new Item.Properties().tab(itemGroup));
    }
}
