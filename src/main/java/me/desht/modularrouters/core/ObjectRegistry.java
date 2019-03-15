package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.BlockTemplateFrame;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.block.tile.TileEntityTemplateFrame;
import me.desht.modularrouters.item.augment.*;
import me.desht.modularrouters.item.module.*;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.item.smartfilter.InspectionFilter;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.item.smartfilter.RegexFilter;
import me.desht.modularrouters.item.upgrade.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.RL;

@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ObjectRegistry {
    private static final ItemGroup MR_CREATIVE_TAB = new ItemGroup(ModularRouters.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ITEM_ROUTER);
        }
    };

    public static final List<IForgeRegistryEntry> registeredObjects = new ArrayList<>();

    // Blocks
    @ObjectHolder("modularrouters:item_router")
    public static final BlockItemRouter ITEM_ROUTER = null;
    @ObjectHolder("modularrouters:template_frame")
    public static final BlockTemplateFrame TEMPLATE_FRAME = null;

    // Items
    @ObjectHolder("modularrouters:blank_module")
    public static final Item BLANK_MODULE = null;
    @ObjectHolder("modularrouters:blank_upgrade")
    public static final Item BLANK_UPGRADE = null;
    @ObjectHolder("modularrouters:override_card")
    public static final Item OVERRIDE_CARD = null;
    @ObjectHolder("modularrouters:augment_core")
    public static final Item AUGMENT_CORE = null;

    @ObjectHolder("modularrouters:activator_module")
    public static final Item ACTIVATOR_MODULE = null;
    @ObjectHolder("modularrouters:breaker_module")
    public static final Item BREAKER_MODULE = null;
    @ObjectHolder("modularrouters:detector_module")
    public static final Item DETECTOR_MODULE = null;
    @ObjectHolder("modularrouters:distributor_module")
    public static final Item DISTRIBUTOR_MODULE = null;
    @ObjectHolder("modularrouters:dropper_module")
    public static final Item DROPPER_MODULE = null;
    @ObjectHolder("modularrouters:extruder_module_1")
    public static final Item EXTRUDER_MODULE_1 = null;
    @ObjectHolder("modularrouters:extruder_module_2")
    public static final Item EXTRUDER_MODULE_2 = null;
    @ObjectHolder("modularrouters:flinger_module")
    public static final Item FLINGER_MODULE = null;
    @ObjectHolder("modularrouters:fluid_module")
    public static final Item FLUID_MODULE = null;
    @ObjectHolder("modularrouters:placer_module")
    public static final Item PLACER_MODULE = null;
    @ObjectHolder("modularrouters:puller_module_1")
    public static final Item PULLER_MODULE_1 = null;
    @ObjectHolder("modularrouters:puller_module_2")
    public static final Item PULLER_MODULE_2 = null;
    @ObjectHolder("modularrouters:sender_module_1")
    public static final Item SENDER_MODULE_1 = null;
    @ObjectHolder("modularrouters:sender_module_2")
    public static final Item SENDER_MODULE_2 = null;
    @ObjectHolder("modularrouters:sender_module_3")
    public static final Item SENDER_MODULE_3 = null;
    @ObjectHolder("modularrouters:vacuum_module")
    public static final Item VACUUM_MODULE = null;
    @ObjectHolder("modularrouters:void_module")
    public static final Item VOID_MODULE = null;

    @ObjectHolder("modularrouters:blast_upgrade")
    public static final Item BLAST_UPGRADE = null;
    @ObjectHolder("modularrouters:camouflage_upgrade")
    public static final Item CAMOUFLAGE_UPGRADE = null;
    @ObjectHolder("modularrouters:fluid_upgrade")
    public static final Item FLUID_UPGRADE = null;
    @ObjectHolder("modularrouters:muffler_upgrade")
    public static final Item MUFFLER_UPGRADE = null;
    @ObjectHolder("modularrouters:security_upgrade")
    public static final Item SECURITY_UPGRADE = null;
    @ObjectHolder("modularrouters:speed_upgrade")
    public static final Item SPEED_UPGRADE = null;
    @ObjectHolder("modularrouters:stack_upgrade")
    public static final Item STACK_UPGRADE = null;
    @ObjectHolder("modularrouters:sync_upgrade")
    public static final Item SYNC_UPGRADE = null;

    @ObjectHolder("modularrouters:fast_pickup_augment")
    public static final Item FAST_PICKUP_AUGMENT = null;
    @ObjectHolder("modularrouters:mimic_augment")
    public static final Item MIMIC_AUGMENT = null;
    @ObjectHolder("modularrouters:pickup_delay_augment")
    public static final Item PICKUP_DELAY_AUGMENT = null;
    @ObjectHolder("modularrouters:pushing_augment")
    public static final Item PUSHING_AUGMENT = null;
    @ObjectHolder("modularrouters:range_up_augment")
    public static final Item RANGE_UP_AUGMENT = null;
    @ObjectHolder("modularrouters:range_down_augment")
    public static final Item RANGE_DOWN_AUGMENT = null;
    @ObjectHolder("modularrouters:redstone_augment")
    public static final Item REDSTONE_AUGMENT = null;
    @ObjectHolder("modularrouters:regulator_augment")
    public static final Item REGULATOR_AUGMENT = null;
    @ObjectHolder("modularrouters:stack_augment")
    public static final Item STACK_AUGMENT = null;
    @ObjectHolder("modularrouters:xp_vacuum_augment")
    public static final Item XP_VACUUM_AUGMENT = null;

    @ObjectHolder("modularrouters:bulk_item_filter")
    public static final Item BULK_ITEM_FILTER = null;
    @ObjectHolder("modularrouters:inspection_filter")
    public static final Item INSPECTION_FILTER = null;
    @ObjectHolder("modularrouters:mod_filter")
    public static final Item MOD_FILTER = null;
    @ObjectHolder("modularrouters:regex_filter")
    public static final Item REGEX_FILTER = null;

    // Sounds
    @ObjectHolder("modularrouters:error")
    public static final SoundEvent SOUND_ERROR = null;
    @ObjectHolder("modularrouters:success")
    public static final SoundEvent SOUND_SUCCESS = null;
    @ObjectHolder("modularrouters:thud")
    public static final SoundEvent SOUND_THUD = null;

    // Tile Entities
    public static final TileEntityType<?> ITEM_ROUTER_TILE = TileEntityType.Builder.create(TileEntityItemRouter::new)
            .build(null).setRegistryName(RL("item_router"));
    public static final TileEntityType<?> TEMPLATE_FRAME_TILE = TileEntityType.Builder.create(TileEntityTemplateFrame::new)
            .build(null).setRegistryName(RL("template_frame"));

    private static Item.Properties ib() {
        return new Item.Properties().group(MR_CREATIVE_TAB);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockItemRouter().setRegistryName(RL("item_router")));
        event.getRegistry().register(new BlockTemplateFrame().setRegistryName(RL("template_frame")));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> r = event.getRegistry();

        // ItemBlocks
        registerObj(r, new ItemBlock(ITEM_ROUTER, ib()), ITEM_ROUTER.getRegistryName());
        registerObj(r, new ItemBlock(TEMPLATE_FRAME, ib()), TEMPLATE_FRAME.getRegistryName());

        // Items
        registerObj(r, new Item(ib()),RL("blank_module"));
        registerObj(r, new Item(ib()),RL("blank_upgrade"));
        registerObj(r, new Item(ib()),RL("augment_core"));
        registerObj(r, new Item(ib()),RL("override_card"));

        registerObj(r, new ActivatorModule(ib()), RL("activator_module"));
        registerObj(r, new BreakerModule(ib()), RL("breaker_module"));
        registerObj(r, new DetectorModule(ib()), RL("detector_module"));
        registerObj(r, new DistributorModule(ib()), RL("distributor_module"));
        registerObj(r, new DropperModule(ib()), RL("dropper_module"));
        registerObj(r, new ExtruderModule1(ib()), RL("extruder_module_1"));
        registerObj(r, new ExtruderModule2(ib()), RL("extruder_module_2"));
        registerObj(r, new FlingerModule(ib()), RL("flinger_module"));
        registerObj(r, new FluidModule(ib()), RL("fluid_module"));
        registerObj(r, new PlacerModule(ib()), RL("placer_module"));
        registerObj(r, new PlayerModule(ib()), RL("player_module"));
        registerObj(r, new PullerModule1(ib()), RL("puller_module_1"));
        registerObj(r, new PullerModule2(ib()), RL("puller_module_2"));
        registerObj(r, new SenderModule1(ib()), RL("sender_module_1"));
        registerObj(r, new SenderModule2(ib()), RL("sender_module_2"));
        registerObj(r, new SenderModule3(ib()), RL("sender_module_3"));
        registerObj(r, new VacuumModule(ib()), RL("vacuum_module"));
        registerObj(r, new VoidModule(ib()), RL("void_module"));

        registerObj(r, new BlastUpgrade(ib()), RL("blast_upgrade"));
        registerObj(r, new CamouflageUpgrade(ib()), RL("camouflage_upgrade"));
        registerObj(r, new FluidUpgrade(ib()), RL("fluid_upgrade"));
        registerObj(r, new MufflerUpgrade(ib()), RL("muffler_upgrade"));
        registerObj(r, new SecurityUpgrade(ib()), RL("security_upgrade"));
        registerObj(r, new SpeedUpgrade(ib()), RL("speed_upgrade"));
        registerObj(r, new StackUpgrade(ib()), RL("stack_upgrade"));
        registerObj(r, new SyncUpgrade(ib()), RL("sync_upgrade"));

        registerObj(r, new FastPickupAugment(ib()), RL("fast_pickup_augment"));
        registerObj(r, new MimicAugment(ib()), RL("mimic_augment"));
        registerObj(r, new PickupDelayAugment(ib()), RL("pickup_delay_augment"));
        registerObj(r, new PushingAugment(ib()), RL("pushing_augment"));
        registerObj(r, new RangeAugments.RangeUpAugment(ib()), RL("range_up_augment"));
        registerObj(r, new RangeAugments.RangeDownAugment(ib()), RL("range_down_augment"));
        registerObj(r, new RedstoneAugment(ib()), RL("redstone_augment"));
        registerObj(r, new RegulatorAugment(ib()), RL("regulator_augment"));
        registerObj(r, new StackAugment(ib()), RL("stack_augment"));
        registerObj(r, new XPVacuumAugment(ib()), RL("xp_vacuum_augment"));

        registerObj(r, new BulkItemFilter(ib()), RL("bulk_item_filter"));
        registerObj(r, new InspectionFilter(ib()), RL("inspection_filter"));
        registerObj(r, new ModFilter(ib()), RL("mod_filter"));
        registerObj(r, new RegexFilter(ib()), RL("regex_filter"));
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        registerObj(event.getRegistry(), new SoundEvent(RL("error")), RL("error"));
        registerObj(event.getRegistry(), new SoundEvent(RL("success")), RL("success"));
        registerObj(event.getRegistry(), new SoundEvent(RL("thud")), RL("thud"));
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(ITEM_ROUTER_TILE);
        event.getRegistry().register(TEMPLATE_FRAME_TILE);
    }

    private static <V extends IForgeRegistryEntry<V>> void registerObj(IForgeRegistry<V> registry, IForgeRegistryEntry<V> o, ResourceLocation name)
    {
        registry.register(o.setRegistryName(name));
        registeredObjects.add(o);
    }

//    @OnlyIn(Dist.CLIENT)
//    @SubscribeEvent
//    public static void registerSubItemModels(ModelRegistryEvent event) {
//        // the can_emit property has no effect on block rendering, so let's not create unnecessary variants
//        ModelLoader.setCustomStateMapper(ObjectRegistry.ITEM_ROUTER, new StateMap.Builder().ignore(BlockItemRouter.CAN_EMIT).build());
//
//        StateMapperBase ignoreState = new StateMapperBase() {
//            @Nonnull
//            @Override
//            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState iBlockState) {
//                return TemplateFrameModel.VARIANT_TAG;
//            }
//        };
//        ModelLoader.setCustomStateMapper(ObjectRegistry.TEMPLATE_FRAME, ignoreState);
//
//        registerSimpleModels(Item.getItemFromBlock(ITEM_ROUTER), Item.getItemFromBlock(TEMPLATE_FRAME));
//        registerSimpleModels(BLANK_MODULE, BLANK_UPGRADE, AUGMENT_CORE, OVERRIDE_CARD);
//        registerSubItemModels(MODULE, UPGRADE, FILTER, AUGMENT);
//    }
//
//    private static void registerSimpleModels(Item... items) {
//        for (Item item : items) {
//            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
//        }
//    }
//
//    private static void registerSubItemModels(ItemSubTypes... items) {
//        for (ItemSubTypes item : items) {
//            for (int i = 0; i < item.getSubTypes(); i++) {
//                ModelLoader.setCustomModelResourceLocation(item, i,
//                        new ModelResourceLocation(item.getRegistryName() + "/" + item.getSubTypeName(i), "inventory"));
//            }
//        }
//    }
}
