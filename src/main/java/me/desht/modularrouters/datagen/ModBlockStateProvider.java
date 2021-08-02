package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.core.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    private static final Direction[] HORIZONTALS = new Direction[] {
        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    public ModBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, ModularRouters.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        String routerName = ModBlocks.MODULAR_ROUTER.get().getRegistryName().getPath();
        models().withExistingParent(routerName,"block/block")
                .texture("back", modid("block/%s_back", routerName))
                .texture("side", modid("block/%s_side", routerName))
                .texture("top", modid("block/%s_top", routerName))
                .texture("bottom", modid("block/%s_bottom", routerName))
                .texture("particle", modid("block/%s_other", routerName))
                .element()
                .face(Direction.DOWN).texture("#bottom").cullface(Direction.DOWN).end()
                .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                .face(Direction.NORTH).texture("#front").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#back").cullface(Direction.SOUTH).end()
                .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                .end();
        ModelFile routerOff = models().withExistingParent(routerName + "_off", modid("block/%s", routerName))
                .texture("front", modid("block/%s_front", routerName));
        ModelFile routerOn = models().withExistingParent(routerName + "_on", modid("block/%s", routerName))
                .texture("front", modid("block/%s_front_active", routerName));

        VariantBlockStateBuilder.PartialBlockstate builder = getVariantBuilder(ModBlocks.MODULAR_ROUTER.get()).partialState();
        for (Direction d : HORIZONTALS) {
            builder.with(ModularRouterBlock.ACTIVE, false).with(ModularRouterBlock.FACING, d)
                    .setModels(new ConfiguredModel(routerOff, 0, getYRotation(d), false));
            builder.with(ModularRouterBlock.ACTIVE, true).with(ModularRouterBlock.FACING, d)
                    .setModels(new ConfiguredModel(routerOn, 0, getYRotation(d), false));
        }

        simpleBlock(ModBlocks.TEMPLATE_FRAME.get());

        simpleBlockItem(ModBlocks.MODULAR_ROUTER.get(), routerOff);
    }

    private int getYRotation(Direction d) {
        return switch (d) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> throw new IllegalArgumentException("invalid dir");
        };
    }

    @Override
    public String getName() {
        return "Modular Routers Blockstates";
    }

    static String modid(String s, Object... args) {
        return ModularRouters.MODID + ":" + String.format(s, args);
    }
}
