package me.desht.modularrouters.datagen;

import com.mojang.math.Quadrant;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.ModularRouterBlock;
import me.desht.modularrouters.client.item.DistributorModeProperty;
import me.desht.modularrouters.client.item.ModuleTintSource;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.DistributorModule;
import me.desht.modularrouters.item.module.EnergyDistributorModule;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import me.desht.modularrouters.logic.settings.TransferDirection;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.registries.DeferredHolder;

import static net.minecraft.client.data.models.BlockModelGenerators.variant;
import static net.minecraft.client.data.models.ItemModelGenerators.BLANK_LAYER;
import static net.minecraft.client.data.models.model.TextureMapping.getBlockTexture;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, ModularRouters.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        ModularRouterBlock routerBlock = ModBlocks.MODULAR_ROUTER.get();

        var routerTemplate = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .element(elementBuilder -> {
                    elementBuilder.face(Direction.DOWN, faceBuilder -> faceBuilder.cullface(Direction.DOWN).texture(TextureSlot.BOTTOM));
                    elementBuilder.face(Direction.UP, faceBuilder -> faceBuilder.cullface(Direction.UP).texture(TextureSlot.TOP));
                    elementBuilder.face(Direction.NORTH, faceBuilder -> faceBuilder.cullface(Direction.NORTH).texture(TextureSlot.FRONT));
                    elementBuilder.face(Direction.SOUTH, faceBuilder -> faceBuilder.cullface(Direction.SOUTH).texture(TextureSlot.BACK));
                    elementBuilder.face(Direction.WEST, faceBuilder -> faceBuilder.cullface(Direction.WEST).texture(TextureSlot.SIDE));
                    elementBuilder.face(Direction.EAST, faceBuilder -> faceBuilder.cullface(Direction.EAST).texture(TextureSlot.SIDE));
                })
                .requiredTextureSlot(TextureSlot.BOTTOM)
                .requiredTextureSlot(TextureSlot.TOP)
                .requiredTextureSlot(TextureSlot.FRONT)
                .requiredTextureSlot(TextureSlot.BACK)
                .requiredTextureSlot(TextureSlot.SIDE)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .build();

        var routerOff = routerTemplate.createWithSuffix(routerBlock, "_off", routerTex(routerBlock, false), blockModels.modelOutput);
        var routerOn = routerTemplate.createWithSuffix(routerBlock, "_on", routerTex(routerBlock, true), blockModels.modelOutput);

//        PropertyDispatch<VariantMutator> horiz = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
//                        .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
//                        .select(Direction.NORTH, BlockModelGenerators.NOP)
//                        .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
//                        .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180);

        Variant offVariant = new Variant(routerOff);
        Variant onVariant = new Variant(routerOn);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(routerBlock).with(PropertyDispatch.initial(
                                BlockStateProperties.HORIZONTAL_FACING, ModularRouterBlock.ACTIVE)
                        .select(Direction.EAST, false, variant(offVariant.withYRot(Quadrant.R90)))
                        .select(Direction.EAST, true, variant(onVariant.withYRot(Quadrant.R90)))
                        .select(Direction.NORTH, false, variant(offVariant.withYRot(Quadrant.R0)))
                        .select(Direction.NORTH, true, variant(onVariant.withYRot(Quadrant.R0)))
                        .select(Direction.WEST, false, variant(offVariant.withYRot(Quadrant.R270)))
                        .select(Direction.WEST, true, variant(onVariant.withYRot(Quadrant.R270)))
                        .select(Direction.SOUTH, false, variant(offVariant.withYRot(Quadrant.R180)))
                        .select(Direction.SOUTH, true, variant(onVariant.withYRot(Quadrant.R180)))

                )
        );

//        blockModels.blockStateOutput.accept(
//                MultiVariantGenerator.multiVariant(routerBlock)
//                        .with(PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, ModularRouterBlock.ACTIVE)
//                                .select(Direction.EAST, false, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOff)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
//                                .select(Direction.EAST, true, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOn)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
//                                .select(Direction.NORTH, false, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOff)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0))
//                                .select(Direction.NORTH, true, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOn)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0))
//                                .select(Direction.WEST, false, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOff)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
//                                .select(Direction.WEST, true, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOn)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
//                                .select(Direction.SOUTH, false, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOff)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
//                                .select(Direction.SOUTH, true, Variant.variant()
//                                        .with(VariantProperties.MODEL, routerOn)
//                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
//                        )
//        );

        blockModels.registerSimpleItemModel(ModBlocks.MODULAR_ROUTER.asItem(), modLocation("block/modular_router_off"));

        blockModels.createTrivialCube(ModBlocks.TEMPLATE_FRAME.get());

        for (var registryObject : ModItems.ITEMS.getEntries()) {
            String name = registryObject.getId().getPath();
            switch (registryObject.get()) {
                case ModuleItem moduleItem -> {
                    if (moduleItem instanceof DistributorModule || moduleItem instanceof EnergyDistributorModule) {
                        ItemModel.Unbaked distPush = makeThreeLayerTintedModel(itemModels, registryObject, "");
                        ItemModel.Unbaked distPull = makeThreeLayerTintedModel(itemModels, registryObject, "_pull");
                        itemModels.itemModelOutput.accept(moduleItem, ItemModelUtils.select(
                                DistributorModeProperty.INSTANCE,
                                distPush,
                                ItemModelUtils.when(TransferDirection.FROM_ROUTER, distPush),
                                ItemModelUtils.when(TransferDirection.TO_ROUTER, distPull)
                        ));
                    } else {
                        moduleItem(itemModels, registryObject);
                    }
                }
                case UpgradeItem ignored -> upgradeItem(itemModels, registryObject);
                case AugmentItem ignored -> augmentItem(itemModels, registryObject, name);
                case SmartFilterItem ignored -> filterItem(itemModels, registryObject, name);
                default -> {}
            }
        }

        itemModels.itemModelOutput.accept(ModItems.BLANK_MODULE.get(), ItemModelUtils.plainModel(
                itemModels.generateLayeredItem(ModItems.BLANK_MODULE.get(),
                        new Material(modLocation("item/module/module_layer0")),
                        new Material(modLocation("item/module/module_layer1"))
                ))
        );
        itemModels.itemModelOutput.accept(ModItems.BLANK_UPGRADE.get(), ItemModelUtils.plainModel(
                itemModels.generateLayeredItem(ModItems.BLANK_UPGRADE.get(),
                        new Material(modLocation("item/upgrade/upgrade_layer0")),
                        new Material(modLocation("item/upgrade/upgrade_layer1"))
                ))
        );
        itemModels.generateFlatItem(ModItems.AUGMENT_CORE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.OVERRIDE_CARD.get(), ModelTemplates.FLAT_ITEM);
    }

    private static TextureMapping routerTex(Block block, boolean active) {
        return new TextureMapping()
                .put(TextureSlot.PARTICLE, getBlockTexture(block, "_front"))
                .put(TextureSlot.SIDE, getBlockTexture(block, "_side"))
                .put(TextureSlot.FRONT, getBlockTexture(block, active ? "_front_active" : "_front"))
                .put(TextureSlot.BACK, getBlockTexture(block, "_back"))
                .put(TextureSlot.TOP, getBlockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
    }

    private void moduleItem(ItemModelGenerators gen, DeferredHolder<Item, ? extends Item> holder) {
        tintedItem3Layers(gen, holder);
    }

    private void upgradeItem(ItemModelGenerators gen, DeferredHolder<Item, ? extends Item> holder) {
        tintedItem3Layers(gen, holder);
    }

    private void filterItem(ItemModelGenerators gen, DeferredHolder<Item, ? extends Item> registryObject, String name) {
        TextureMapping mapping = TextureMapping.layer0(new Material(modLocation("item/filter/" + name)));
        var loc = ModelTemplates.FLAT_ITEM.create(registryObject.get(), mapping, gen.modelOutput);
        gen.itemModelOutput.accept(registryObject.get(), ItemModelUtils.plainModel(loc));
    }

    private void augmentItem(ItemModelGenerators gen, DeferredHolder<Item, ? extends Item> registryObject, String name) {
        var loc = gen.generateLayeredItem(modLocation("item/augment/" + name),
                new Material(modLocation("item/augment/augment_layer0")),
                new Material(modLocation("item/augment/" + name)));
        gen.itemModelOutput.accept(registryObject.get(), ItemModelUtils.plainModel(loc));
    }

    private void tintedItem3Layers(ItemModelGenerators gen, DeferredHolder<Item, ? extends Item> holder) {
        gen.itemModelOutput.accept(holder.get(), makeThreeLayerTintedModel(gen, holder, ""));
    }

    private ItemModel.Unbaked makeThreeLayerTintedModel(ItemModelGenerators gen, DeferredHolder<Item, ? extends Item> holder, String suffix) {
        String what;
        switch (holder.get()) {
            case ModuleItem ignored -> what = "module";
            case UpgradeItem ignored -> what = "upgrade";
            default -> throw new IllegalArgumentException("expecting module or upgrade!");
        }
        String name = holder.getId().getPath();
        String loc = "item/" + what + "/" + name + suffix;
        var modelLoc = ModelTemplates.THREE_LAYERED_ITEM.create(modLocation(loc), TextureMapping.layered(
                new Material( modLocation(String.format("item/%s/%s_layer0", what, what))),
                new Material(modLocation(String.format("item/%s/%s_layer1", what, what))),
                new Material( modLocation(loc))
        ), gen.modelOutput);
        return ItemModelUtils.tintedModel(modelLoc, BLANK_LAYER, ModuleTintSource.INSTANCE, BLANK_LAYER);
    }
}
