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
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter.FilterType;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.modularrouters.util.MiscUtil.translate;

@GuideBook
public class Guidebook implements IGuideBook {
    public static Book guideBook;

    @Nullable
    @Override
    public Book buildBook() {
        Map<ResourceLocation, EntryAbstract> entries;
        List<CategoryAbstract> categories = new ArrayList<>();
        List<IPage> pages;

        // Intro category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.introText"), 250));
        entries.put(new ResourceLocation(ModularRouters.modId, "intro"),
                new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(ModBlocks.itemRouter)));
        categories.add(new CategoryItemStack(entries, translate("guidebook.categories.introduction"), new ItemStack(Items.WRITABLE_BOOK)));

        // Routers category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.routerText"), 250));
        pages.add(new PageIRecipe(
                        new ShapedOreRecipe(new ItemStack(ModBlocks.itemRouter, 4),
                                "ibi", "bmb", "ibi", 'b', Blocks.IRON_BARS, 'i', Items.IRON_INGOT, 'm', ModItems.blankModule)
                )
        );
        entries.put(new ResourceLocation(ModularRouters.modId, "router"),
                new EntryItemStack(pages, translate("tile.itemRouter.name"), new ItemStack(ModBlocks.itemRouter)));
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.routerEcoMode", ModuleType.values().length, String.valueOf(Config.configKey)), 250));
        entries.put(new ResourceLocation(ModularRouters.modId, "routerEcoMode"),
                new EntryItemStack(pages, translate("guidebook.words.ecoMode"), new ItemStack(Blocks.SAPLING)));
        categories.add(new CategoryItemStack(entries, translate("guidebook.categories.routers"), new ItemStack(ModBlocks.itemRouter)));

        // Modules category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.moduleOverview", ModuleType.values().length, String.valueOf(Config.configKey)), 250));
        entries.put(new ResourceLocation(ModularRouters.modId, "moduleOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.moduleRedstone", ModuleType.values().length, String.valueOf(Config.configKey)), 250));
        pages.add(new PageIRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.blankModule),
                        " r ", "tmt", " r ", 'r', Items.REDSTONE, 'm', ModItems.blankModule, 't', Blocks.REDSTONE_TORCH)
        ));
        entries.put(new ResourceLocation(ModularRouters.modId, "moduleRedstone"), new EntryItemStack(pages, translate("guidebook.words.redstone"), new ItemStack(Items.REDSTONE)));
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.moduleRegulator", ModuleType.values().length, String.valueOf(Config.configKey)), 250));
        pages.add(new PageIRecipe(
                new ShapedOreRecipe(new ItemStack(ModItems.blankModule),
                        " q ", "cmc", " q ",
                        'q', Items.QUARTZ, 'm', ModItems.blankModule, 'c', Items.COMPARATOR)
        ));
        entries.put(new ResourceLocation(ModularRouters.modId, "moduleRegulator"), new EntryItemStack(pages, translate("guidebook.words.regulator"), new ItemStack(Items.QUARTZ)));
        pages = Arrays.asList(
                new PageText(translate("guidebook.para.blankModule")),
                new PageIRecipe(new ShapedOreRecipe(new ItemStack(ModItems.blankModule, 6),
                        " r ", "ppp", "nnn", 'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET)
                )
        );
        ItemStack bm = new ItemStack(ModItems.blankModule);
        entries.put(new ResourceLocation(ModularRouters.modId, "blankModule"), new EntryItemStack(pages, translate(bm.getUnlocalizedName() + ".name"), bm));
        buildModulePages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.modules"), new ItemStack(ModItems.blankModule)));

        // Upgrades category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.upgradeOverview", UpgradeType.values().length, TileEntityItemRouter.N_UPGRADE_SLOTS), 250));
        entries.put(new ResourceLocation(ModularRouters.modId, "upgradeOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        pages = Arrays.asList(
                new PageText(translate("guidebook.para.blankUpgrade")),
                new PageIRecipe(new ShapedOreRecipe(new ItemStack(ModItems.blankUpgrade, 6),
                        "ppn", "pdn", " pn", 'p', Items.PAPER, 'd', Items.DIAMOND, 'n', Items.GOLD_NUGGET))

        );
        ItemStack bu = new ItemStack(ModItems.blankUpgrade);
        entries.put(new ResourceLocation(ModularRouters.modId, "blankUpgrade"), new EntryItemStack(pages, translate(bu.getUnlocalizedName() + ".name"), bu));
        buildUpgradePages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.upgrades"), new ItemStack(ModItems.blankUpgrade)));

        // Filters category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.filterOverview", FilterType.values().length), 250));
        entries.put(new ResourceLocation(ModularRouters.modId, "filterOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        buildFilterPages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.filters"), ItemSmartFilter.makeItemStack(FilterType.BULKITEM)));

        // and done
        guideBook = new Book();
        guideBook.setAuthor("desht");
        guideBook.setTitle("Modular Routers Guide");
        guideBook.setDisplayName("Modular Routers Guide");
        guideBook.setColor(Color.CYAN);
        guideBook.setCategoryList(categories);
        guideBook.setRegistryName(new ResourceLocation(ModularRouters.modId, "guidebook"));
        guideBook.setSpawnWithBook(Config.startWithGuide);

        return guideBook;
    }

    private static void buildFilterPages(Map<ResourceLocation, EntryAbstract> entries) {
        List<FilterType> types = Lists.newArrayList(FilterType.values()).stream()
                .map(ItemSmartFilter::makeItemStack)
                .sorted((s1, s2) -> translate(s1.getUnlocalizedName()).compareTo(translate(s2.getUnlocalizedName())))
                .map(FilterType::getType)
                .collect(Collectors.toList());
        for (FilterType type : types) {
            ItemStack module = ItemSmartFilter.makeItemStack(type);
            String unlocalizedName = module.getItem().getUnlocalizedName(module);
            List<IPage> pages1 = Lists.newArrayList();
            pages1.add(new PageText(translate("itemText.usage." + unlocalizedName)));
            pages1.add(new PageIRecipe(ItemSmartFilter.getFilter(type).getRecipe()));
            String localizedName = translate(unlocalizedName + ".name");
            entries.put(new ResourceLocation(ModularRouters.modId, unlocalizedName),
                    new EntryItemStack(pages1, localizedName, module));
        }
    }

    private static void buildModulePages(Map<ResourceLocation, EntryAbstract> entries) {
        // get a items of modules, sorted by their localized names
        List<ModuleType> types = Lists.newArrayList(ModuleType.values()).stream()
                .map(ItemModule::makeItemStack)
                .sorted((s1, s2) -> translate(s1.getUnlocalizedName()).compareTo(translate(s2.getUnlocalizedName())))
                .map(ModuleType::getType)
                .collect(Collectors.toList());

        for (ModuleType type : types) {
            ItemStack module = ItemModule.makeItemStack(type);
            String unlocalizedName = module.getItem().getUnlocalizedName(module);
            List<IPage> pages1 = Lists.newArrayList();
            pages1.add(new PageText(translate("itemText.usage." + unlocalizedName, ItemModule.getModule(type).getExtraUsageParams())));
            pages1.add(new PageIRecipe(ItemModule.getModule(type).getRecipe()));
            String localizedName = translate(unlocalizedName + ".name");
            entries.put(new ResourceLocation(ModularRouters.modId, unlocalizedName),
                    new EntryItemStack(pages1, localizedName, module));
        }
    }

    private static void buildUpgradePages(Map<ResourceLocation, EntryAbstract> entries) {
        // get a items of upgrades, sorted by their localized names
        List<UpgradeType> types = Lists.newArrayList(UpgradeType.values()).stream()
                .map(ItemUpgrade::makeItemStack)
                .sorted((s1, s2) -> translate(s1.getUnlocalizedName()).compareTo(translate(s2.getUnlocalizedName())))
                .map(UpgradeType::getType)
                .collect(Collectors.toList());

        for (UpgradeType type : types) {
            ItemStack upgrade = ItemUpgrade.makeItemStack(type);
            String unlocalizedName = upgrade.getItem().getUnlocalizedName(upgrade);
            List<IPage> pages1 = Lists.newArrayList();
            pages1.add(new PageText(translate("itemText.usage." + unlocalizedName, ItemUpgrade.getUpgrade(type).getExtraUsageParams())));
            pages1.add(new PageIRecipe(ItemUpgrade.getUpgrade(type).getRecipe()));
            String localizedName = translate(unlocalizedName + ".name");
            entries.put(new ResourceLocation(ModularRouters.modId, unlocalizedName),
                    new EntryItemStack(pages1, localizedName, upgrade));
        }
    }
    @Override
    public void handleModel(ItemStack bookStack) {
        GuideAPI.setModel(guideBook);
    }

    @Override
    public void handlePost(ItemStack bookStack) {
        GameRegistry.addShapelessRecipe(bookStack, ModItems.blankModule, Items.BOOK);
    }
}
