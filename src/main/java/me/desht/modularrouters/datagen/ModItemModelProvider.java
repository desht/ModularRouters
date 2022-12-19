package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

import static me.desht.modularrouters.datagen.ModBlockStateProvider.modid;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), ModularRouters.MODID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Modular Routers Item Models";
    }

    @Override
    protected void registerModels() {
        for (RegistryObject<? extends Item> registryObject : ModItems.REGISTRY_OBJECTS) {
            String name = registryObject.getId().getPath();
            Item item = registryObject.get();
            if (item instanceof ModuleItem) {
                if (item == ModItems.DISTRIBUTOR_MODULE.get()) {
                    // special case; distributor module has a model override based on its mode
                    ModelFile distributorPull = simpleItemVariant(ModItems.DISTRIBUTOR_MODULE, "_pull",
                            modid("item/module/module_layer0"),
                            modid("item/module/module_layer1"),
                            modid("item/module/distributor_module_pull"));
                    simpleItem(ModItems.DISTRIBUTOR_MODULE,
                            modid("item/module/module_layer0"),
                            modid("item/module/module_layer1"),
                            modid("item/module/distributor_module"))
                            .override().predicate(modLoc("mode"), 0.5f).model(distributorPull);
                } else {
                    simpleItem(registryObject,
                            modid("item/module/module_layer0"),
                            modid("item/module/module_layer1"),
                            modid("item/module/" + name));
                }
            } else if (item instanceof UpgradeItem) {
                simpleItem(registryObject,
                        modid("item/upgrade/upgrade_layer0"),
                        modid("item/upgrade/upgrade_layer1"),
                        modid("item/upgrade/" + name));
            } else if (item instanceof AugmentItem) {
                simpleItem(registryObject,
                        modid("item/augment/augment_layer0"),
                        modid("item/augment/" + name));
            } else if (item instanceof SmartFilterItem) {
                simpleItem(registryObject, modid("item/filter/" + name));
            }
        }

        simpleItem(ModItems.BLANK_MODULE, modid("item/module/module_layer0"), modid("item/module/module_layer1"));
        simpleItem(ModItems.BLANK_UPGRADE, modid("item/upgrade/upgrade_layer0"), modid("item/upgrade/upgrade_layer1"));
        simpleItem(ModItems.AUGMENT_CORE, modid("item/augment/augment_layer0"));
        simpleItem(ModItems.OVERRIDE_CARD, modid("item/override_card"));

        withExistingParent("manual", GENERATED).texture("layer0", modid("item/manual"));
    }

    private ItemModelBuilder simpleItem(RegistryObject<? extends Item> item, String... textures) {
        return simpleItem(item.getId(), textures);
    }

    private ItemModelBuilder simpleItem(ResourceLocation itemKey, String... textures) {
        ItemModelBuilder builder = withExistingParent(itemKey.getPath(), GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }

    private ItemModelBuilder simpleItemVariant(RegistryObject<? extends Item> item, String suffix, String... textures) {
        ItemModelBuilder builder = withExistingParent(item.getId().getPath() + suffix, GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }
}
