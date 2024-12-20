package me.desht.modularrouters.datagen;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.module.ModuleItem;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;

import static me.desht.modularrouters.datagen.ModBlockStateProvider.modid;

public class ModItemModelProvider extends ItemModelProvider {
    private static final ResourceLocation GENERATED = ResourceLocation.parse("item/generated");

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModularRouters.MODID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Modular Routers Item Models";
    }

    @Override
    protected void registerModels() {
        for (var registryObject : ModItems.ITEMS.getEntries()) {
            String name = registryObject.getId().getPath();
            switch (registryObject.get()) {
                case ModuleItem moduleItem -> {
                    simpleItem(registryObject,
                            modid("item/module/module_layer0"),
                            modid("item/module/module_layer1"),
                            modid("item/module/" + name));
                    if (moduleItem.getDefaultInstance().has(ModDataComponents.DISTRIBUTOR_SETTINGS)) {
                        simpleItemVariant(registryObject, "_pull",
                                modid("item/module/module_layer0"),
                                modid("item/module/module_layer1"),
                                modid("item/module/%s_pull", name));
                    }
                }
                case UpgradeItem ignored -> simpleItem(registryObject,
                        modid("item/upgrade/upgrade_layer0"),
                        modid("item/upgrade/upgrade_layer1"),
                        modid("item/upgrade/" + name));
                case AugmentItem ignored -> simpleItem(registryObject,
                        modid("item/augment/augment_layer0"),
                        modid("item/augment/" + name));
                case SmartFilterItem ignored -> simpleItem(registryObject, modid("item/filter/" + name));
                default -> {}
            }
        }

        simpleItem(ModItems.BLANK_MODULE, modid("item/module/module_layer0"), modid("item/module/module_layer1"));
        simpleItem(ModItems.BLANK_UPGRADE, modid("item/upgrade/upgrade_layer0"), modid("item/upgrade/upgrade_layer1"));
        simpleItem(ModItems.AUGMENT_CORE, modid("item/augment/augment_layer0"));
        simpleItem(ModItems.OVERRIDE_CARD, modid("item/override_card"));

        withExistingParent("manual", GENERATED).texture("layer0", modid("item/manual"));
    }

    private ItemModelBuilder simpleItem(DeferredHolder<Item, ? extends Item> item, String... textures) {
        return simpleItem(item.getId(), textures);
    }

    private ItemModelBuilder simpleItem(ResourceLocation itemKey, String... textures) {
        ItemModelBuilder builder = withExistingParent(itemKey.getPath(), GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }

    private ItemModelBuilder simpleItemVariant(DeferredHolder<Item, ? extends Item> item, String suffix, String... textures) {
        ItemModelBuilder builder = withExistingParent(item.getId().getPath() + suffix, GENERATED);
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }
}
