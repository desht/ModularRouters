package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static me.desht.modularrouters.datagen.ModBlockStateProvider.modid;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, ModularRouters.MODID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Modular Routers Item Models";
    }

    @Override
    protected void registerModels() {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            String name = item.getRegistryName().getPath();
            if (item instanceof ItemModule) {
                if (item == ModItems.DISTRIBUTOR_MODULE.get()) {
                    // special case; distributor module has a model override based on its mode
                    ModelFile distributorPull = simpleItemVariant(ModItems.DISTRIBUTOR_MODULE.get(), "_pull",
                            modid("item/module/module_layer0"),
                            modid("item/module/module_layer1"),
                            modid("item/module/distributor_module_pull"));
                    simpleItem(ModItems.DISTRIBUTOR_MODULE.get(),
                            modid("item/module/module_layer0"),
                            modid("item/module/module_layer1"),
                            modid("item/module/distributor_module"))
                            .override().predicate(modLoc("mode"), 0.5f).model(distributorPull);
                } else {
                    simpleItem(item,
                            modid("item/module/module_layer0"),
                            modid("item/module/module_layer1"),
                            modid("item/module/" + name));
                }
            } else if (item instanceof ItemUpgrade) {
                simpleItem(item,
                        modid("item/upgrade/upgrade_layer0"),
                        modid("item/upgrade/upgrade_layer1"),
                        modid("item/upgrade/" + name));
            } else if (item instanceof ItemAugment) {
                simpleItem(item,
                        modid("item/augment/augment_layer0"),
                        modid("item/augment/" + name));
            } else if (item instanceof ItemSmartFilter) {
                simpleItem(item, modid("item/filter/" + name));
            }
        }

        simpleItem(ModItems.BLANK_MODULE, modid("item/module/module_layer0"), modid("item/module/module_layer1"));
        simpleItem(ModItems.BLANK_UPGRADE, modid("item/upgrade/upgrade_layer0"), modid("item/upgrade/upgrade_layer1"));
        simpleItem(ModItems.AUGMENT_CORE, modid("item/augment/augment_layer0"));
        simpleItem(ModItems.OVERRIDE_CARD, modid("item/override_card"));

        withExistingParent("manual", GENERATED).texture("layer0", modid("item/manual"));
    }

    private ItemModelBuilder simpleItem(Supplier<Item> item, String... textures) {
        return simpleItem(item.get(), textures);
    }

    private ItemModelBuilder simpleItem(Item item, String... textures) {
        ItemModelBuilder builder = withExistingParent(item.getRegistryName().getPath(), GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }

    private ItemModelBuilder simpleItemVariant(Item item, String suffix, String... textures) {
        ItemModelBuilder builder = withExistingParent(item.getRegistryName().getPath() + suffix, GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }
}
