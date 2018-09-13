package me.desht.modularrouters.integration.guideapi;


import amerifrance.guideapi.api.GuideAPI;
import amerifrance.guideapi.api.GuideBook;
import amerifrance.guideapi.api.IGuideBook;
import amerifrance.guideapi.api.IPage;
import amerifrance.guideapi.api.impl.Book;
import amerifrance.guideapi.api.impl.abstraction.CategoryAbstract;
import amerifrance.guideapi.api.impl.abstraction.EntryAbstract;
import amerifrance.guideapi.api.util.PageHelper;
import amerifrance.guideapi.category.CategoryItemStack;
import amerifrance.guideapi.entry.EntryItemStack;
import amerifrance.guideapi.page.PageIRecipe;
import amerifrance.guideapi.page.PageText;
import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.augment.ItemAugment.AugmentType;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter.FilterType;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.modularrouters.util.MiscUtil.RL;
import static me.desht.modularrouters.util.MiscUtil.translate;


@GuideBook
public class Guidebook implements IGuideBook {
    private static final int MAX_PAGE_LENGTH = 270;

    private static final int CAT_INTRO = 0;
    private static final int CAT_ROUTER = 1;
    private static final int CAT_MODULES = 2;
    private static final int CAT_UPGRADES = 3;
    private static final int CAT_AUGMENTS = 4;
    private static final int CAT_FILTERS = 5;

    private static Book guideBook;

    @Nullable
    @Override
    public Book buildBook() {
        Map<ResourceLocation, EntryAbstract> entries;
        List<CategoryAbstract> categories = new ArrayList<>();
        List<IPage> pages;

        // Intro category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.introText"), MAX_PAGE_LENGTH));
        entries.put(RL("intro"),
                new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(RegistrarMR.ITEM_ROUTER)));
        categories.add(new CategoryItemStack(entries, translate("guidebook.categories.introduction"), new ItemStack(Items.WRITABLE_BOOK)));

        // Routers category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.routerText"), MAX_PAGE_LENGTH));
        entries.put(RL("router"),
                new EntryItemStack(pages, translate("tile.item_router.name"), new ItemStack(RegistrarMR.ITEM_ROUTER)));
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.routerEcoMode", ModuleType.values().length, String.valueOf(ConfigHandler.getConfigKey())), MAX_PAGE_LENGTH));
        entries.put(RL("routerEcoMode"),
                new EntryItemStack(pages, translate("guidebook.words.ecoMode"), new ItemStack(Blocks.SAPLING)));
        categories.add(new CategoryItemStack(entries, translate("guidebook.categories.routers"), new ItemStack(RegistrarMR.ITEM_ROUTER)));

        // Modules category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.moduleOverview", ModuleType.values().length, String.valueOf(ConfigHandler.getConfigKey())), MAX_PAGE_LENGTH));
        entries.put(RL("moduleOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        pages = Collections.singletonList(new PageText(translate("guidebook.para.blankModule")));
        ItemStack bm = new ItemStack(RegistrarMR.BLANK_MODULE);
        entries.put(RL("blankModule"), new EntryItemStack(pages, translate(bm.getTranslationKey() + ".name"), bm));
        buildModulePages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.modules"), new ItemStack(RegistrarMR.BLANK_MODULE)));

        // Upgrades category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.upgradeOverview", UpgradeType.values().length, new TileEntityItemRouter().getUpgradeSlotCount()), MAX_PAGE_LENGTH));
        entries.put(RL("upgradeOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        pages = Collections.singletonList(new PageText(translate("guidebook.para.blankUpgrade")));
        ItemStack bu = new ItemStack(RegistrarMR.BLANK_UPGRADE);
        entries.put(RL("blankUpgrade"), new EntryItemStack(pages, translate(bu.getTranslationKey() + ".name"), bu));
        buildUpgradePages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.upgrades"), new ItemStack(RegistrarMR.BLANK_UPGRADE)));

        // Augments category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.augmentOverview"), MAX_PAGE_LENGTH));
        entries.put(RL("augmentOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        pages = Collections.singletonList(new PageText(translate("guidebook.para.augmentCore")));
        ItemStack ba = new ItemStack(RegistrarMR.AUGMENT_CORE);
        entries.put(RL("augmentCore"), new EntryItemStack(pages, translate(ba.getTranslationKey() + ".name"), ba));
        buildAugmentPages(entries);
        categories.add(new CategoryItemStack(entries, translate("guidebook.words.augments"), new ItemStack(RegistrarMR.AUGMENT_CORE)));

        // Filters category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.filterOverview", FilterType.values().length), MAX_PAGE_LENGTH));
        entries.put(RL("filterOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        buildFilterPages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.filters"), ItemSmartFilter.makeItemStack(FilterType.BULKITEM)));

        // and done
        guideBook = new Book();
        guideBook.setAuthor("desht");
        guideBook.setTitle("Modular Routers Guide");
        guideBook.setDisplayName("Modular Routers Guide");
        guideBook.setColor(Color.CYAN);
        guideBook.setCategoryList(categories);
        guideBook.setRegistryName(RL("guidebook"));
        guideBook.setSpawnWithBook(ConfigHandler.misc.startWithGuide);

        return guideBook;
    }

    private static void buildAugmentPages(Map<ResourceLocation, EntryAbstract> entries) {
        List<AugmentType> types = Lists.newArrayList(AugmentType.values()).stream()
                .map(ItemAugment::makeItemStack)
                .sorted(Comparator.comparing(s -> translate(s.getTranslationKey())))
                .map(AugmentType::getType)
                .collect(Collectors.toList());
        for (AugmentType type : types) {
            ItemStack module = ItemAugment.makeItemStack(type);
            String unlocalizedName = module.getItem().getTranslationKey(module);
            List<IPage> pages1 = Lists.newArrayList();
            pages1.add(new PageText(translate("itemText.usage." + unlocalizedName)));
            String localizedName = translate(unlocalizedName + ".name");
            entries.put(RL(unlocalizedName), new EntryItemStack(pages1, localizedName, module));
        }
    }

    private static void buildFilterPages(Map<ResourceLocation, EntryAbstract> entries) {
        List<FilterType> types = Lists.newArrayList(FilterType.values()).stream()
                .map(ItemSmartFilter::makeItemStack)
                .sorted(Comparator.comparing(s -> translate(s.getTranslationKey())))
                .map(FilterType::getType)
                .collect(Collectors.toList());
        for (FilterType type : types) {
            ItemStack module = ItemSmartFilter.makeItemStack(type);
            String unlocalizedName = module.getItem().getTranslationKey(module);
            List<IPage> pages1 = Lists.newArrayList();
            pages1.add(new PageText(translate("itemText.usage." + unlocalizedName)));
            String localizedName = translate(unlocalizedName + ".name");
            entries.put(RL(unlocalizedName), new EntryItemStack(pages1, localizedName, module));
        }
    }

    private static void buildModulePages(Map<ResourceLocation, EntryAbstract> entries) {
        // get a items of modules, sorted by their localized names
        List<ModuleType> types = Lists.newArrayList(ModuleType.values()).stream()
                .map(ModuleHelper::makeItemStack)
                .sorted(Comparator.comparing(s -> translate(s.getTranslationKey())))
                .map(ModuleType::getType)
                .collect(Collectors.toList());

        for (ModuleType type : types) {
            ItemStack module = ModuleHelper.makeItemStack(type);
            String unlocalizedName = module.getItem().getTranslationKey(module);
            List<IPage> pages1 = Lists.newArrayList();
            pages1.add(new PageText(translate("itemText.usage." + unlocalizedName)));
            String localizedName = translate(unlocalizedName + ".name");
            entries.put(RL(unlocalizedName), new EntryItemStack(pages1, localizedName, module));
        }
    }

    private static void buildUpgradePages(Map<ResourceLocation, EntryAbstract> entries) {
        // get a items of upgrades, sorted by their localized names
        List<UpgradeType> types = Lists.newArrayList(UpgradeType.values()).stream()
                .map(ItemUpgrade::makeItemStack)
                .sorted(Comparator.comparing(s -> translate(s.getTranslationKey())))
                .map(UpgradeType::getType)
                .collect(Collectors.toList());

        for (UpgradeType type : types) {
            ItemStack upgrade = ItemUpgrade.makeItemStack(type);
            String unlocalizedName = upgrade.getItem().getTranslationKey(upgrade);
            List<IPage> pages1 = Lists.newArrayList();
            pages1.add(new PageText(translate("itemText.usage." + unlocalizedName, ItemUpgrade.getUpgrade(type).getExtraUsageParams())));
            String localizedName = translate(unlocalizedName + ".name");
            entries.put(RL(unlocalizedName), new EntryItemStack(pages1, localizedName, upgrade));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleModel(ItemStack bookStack) {
        GuideAPI.setModel(guideBook);
    }

    @Override
    public void handlePost(ItemStack bookStack) {
        ShapelessOreRecipe bookRecipe = new ShapelessOreRecipe(RL("guidebook"), bookStack, RegistrarMR.BLANK_MODULE, Items.BOOK);
        ForgeRegistries.RECIPES.register(bookRecipe.setRegistryName(new ResourceLocation("guideapi","guidebook")));

        // need to do this here, because recipes aren't registered when buildBook() is called
        addRecipePage(CAT_ROUTER, "router", "item_router");
        addRecipePage(CAT_MODULES, "moduleOverview", "blank_module");
        addRecipePage(CAT_UPGRADES, "upgradeOverview", "blank_upgrade");
        addRecipePage(CAT_AUGMENTS, "augmentOverview", "augment_core");

        addItemRecipes(CAT_MODULES, ModuleType.class, "module");
        addItemRecipes(CAT_UPGRADES, UpgradeType.class, "upgrade");
        addItemRecipes(CAT_AUGMENTS, AugmentType.class, "augment");
        addItemRecipes(CAT_FILTERS, FilterType.class, "filter");
    }

    private void addItemRecipes(int categoryNumber, Class <? extends Enum<?>> c, String tag) {
        for (Enum<?> e : c.getEnumConstants()) {
            String registeredName = e.toString().toLowerCase() + "_" + tag;
            addRecipePage(categoryNumber, "item." + registeredName, tag + "/" + registeredName);
        }
    }

    private void addRecipePage(int categoryNumber, String entryName, String recipeName) {
        IRecipe recipe = CraftingManager.getRecipe(RL(recipeName));
        if (recipe != null) {
            guideBook.getCategoryList()
                    .get(categoryNumber)
                    .getEntry(RL(entryName))
                    .addPage(new PageIRecipe(recipe));
        } else {
            ModularRouters.logger.warn("no recipe found for " + RL(recipeName));
        }
    }
}

