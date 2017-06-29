package me.desht.modularrouters.integration.guideapi;

public class Guidebook {}

/*
@GuideBook
public class Guidebook implements IGuideBook {
    private static final int MAX_PAGE_LENGTH = 270;

    private static final int CAT_INTRO = 0;
    private static final int CAT_ROUTER = 1;
    private static final int CAT_MODULES = 2;
    private static final int CAT_UPGRADES = 3;
    private static final int CAT_ENHANCEMENTS = 4;
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
        entries.put(RL("blankModule"), new EntryItemStack(pages, translate(bm.getUnlocalizedName() + ".name"), bm));
        buildModulePages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.modules"), new ItemStack(RegistrarMR.BLANK_MODULE)));

        // Upgrades category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.upgradeOverview", UpgradeType.values().length, TileEntityItemRouter.N_UPGRADE_SLOTS), MAX_PAGE_LENGTH));
        entries.put(RL("upgradeOverview"), new EntryItemStack(pages, translate("guidebook.words.overview"), new ItemStack(Items.BOOK)));
        pages = Collections.singletonList(new PageText(translate("guidebook.para.blankUpgrade")));
        ItemStack bu = new ItemStack(RegistrarMR.BLANK_UPGRADE);
        entries.put(RL("blankUpgrade"), new EntryItemStack(pages, translate(bu.getUnlocalizedName() + ".name"), bu));
        buildUpgradePages(entries);
        categories.add(new CategoryItemStack(entries, translate("guiText.label.upgrades"), new ItemStack(RegistrarMR.BLANK_UPGRADE)));

        // Enhancements category
        entries = new LinkedHashMap<>();
        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.enhancementsOverview"), MAX_PAGE_LENGTH));
        entries.put(RL("enhancementsOverview"), new EntryItemStack(pages, translate("guidebook.words.enhancements"), new ItemStack(Items.BOOK)));
        buildEnhancementPages(entries);
        categories.add(new CategoryItemStack(entries, translate("guidebook.words.enhancements"), new ItemStack(Blocks.CRAFTING_TABLE)));

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

    private static void buildEnhancementPages(Map<ResourceLocation, EntryAbstract> entries) {
        List<IPage> pages;

        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.fastPickupEnhancement"), MAX_PAGE_LENGTH));
        addEnhancementRecipePage(pages, new FastPickupEnhancementRecipe(ModuleType.VACUUM));
        entries.put(RL("moduleFastPickup"), new EntryItemStack(pages, translate("guidebook.words.fastPickup"), new ItemStack(Items.FISHING_ROD)));

        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.pickupDelayEnhancement"), MAX_PAGE_LENGTH));
        addEnhancementRecipePage(pages, new PickupDelayEnhancementRecipe(ModuleType.DROPPER));
        entries.put(RL("modulePickupDelay"), new EntryItemStack(pages, translate("guidebook.words.pickupDelay"), new ItemStack(Items.SLIME_BALL)));

        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.regulatorEnhancement"), MAX_PAGE_LENGTH));
        addEnhancementRecipePage(pages, new RegulatorEnhancementRecipe(ModuleType.PULLER));
        entries.put(RL("moduleRegulator"), new EntryItemStack(pages, translate("guidebook.words.regulator"), new ItemStack(Items.COMPARATOR)));

        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.redstoneEnhancement"), MAX_PAGE_LENGTH));
        addEnhancementRecipePage(pages, new RedstoneEnhancementRecipe(ModuleType.SENDER1));
        entries.put(RL("moduleRedstone"), new EntryItemStack(pages, translate("guidebook.words.redstone"), new ItemStack(Items.REDSTONE)));

        pages = new ArrayList<>(PageHelper.pagesForLongText(translate("guidebook.para.xpVacuumEnhancement"), MAX_PAGE_LENGTH));
        addEnhancementRecipePage(pages, new XPVacuumEnhancementRecipe(ModuleType.VACUUM));
        entries.put(RL("moduleXPVacuum"), new EntryItemStack(pages, translate("guidebook.words.xpVacuum"), new ItemStack(Items.EXPERIENCE_BOTTLE)));
    }

    private static void addEnhancementRecipePage(List<IPage> pages, ShapedOreRecipe recipe) {
        pages.add(new PageIRecipe(recipe, new ShapedOreRecipeRenderer(recipe)));
    }

    private static void buildFilterPages(Map<ResourceLocation, EntryAbstract> entries) {
        List<FilterType> types = Lists.newArrayList(FilterType.values()).stream()
                .map(ItemSmartFilter::makeItemStack)
                .sorted(Comparator.comparing(s -> translate(s.getUnlocalizedName())))
                .map(FilterType::getType)
                .collect(Collectors.toList());
        for (FilterType type : types) {
            ItemStack module = ItemSmartFilter.makeItemStack(type);
            String unlocalizedName = module.getItem().getUnlocalizedName(module);
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
                .sorted(Comparator.comparing(s -> translate(s.getUnlocalizedName())))
                .map(ModuleType::getType)
                .collect(Collectors.toList());

        for (ModuleType type : types) {
            ItemStack module = ModuleHelper.makeItemStack(type);
            String unlocalizedName = module.getItem().getUnlocalizedName(module);
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
                .sorted(Comparator.comparing(s -> translate(s.getUnlocalizedName())))
                .map(UpgradeType::getType)
                .collect(Collectors.toList());

        for (UpgradeType type : types) {
            ItemStack upgrade = ItemUpgrade.makeItemStack(type);
            String unlocalizedName = upgrade.getItem().getUnlocalizedName(upgrade);
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

        addItemRecipes(CAT_MODULES, ModuleType.class, "module");
        addItemRecipes(CAT_UPGRADES, UpgradeType.class, "upgrade");
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
*/
