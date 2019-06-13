package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.augment.*;
import me.desht.modularrouters.item.module.*;
import me.desht.modularrouters.item.smartfilter.BulkItemFilter;
import me.desht.modularrouters.item.smartfilter.InspectionFilter;
import me.desht.modularrouters.item.smartfilter.ModFilter;
import me.desht.modularrouters.item.smartfilter.RegexFilter;
import me.desht.modularrouters.item.upgrade.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.desht.modularrouters.util.MiscUtil.RL;

@ObjectHolder(ModularRouters.MODID)
@Mod.EventBusSubscriber(modid = ModularRouters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static final Item BLANK_MODULE = null;
    public static final Item BLANK_UPGRADE = null;
    public static final Item OVERRIDE_CARD = null;
    public static final Item AUGMENT_CORE = null;

    public static final Item ACTIVATOR_MODULE = null;
    public static final Item BREAKER_MODULE = null;
    public static final Item DETECTOR_MODULE = null;
    public static final Item DISTRIBUTOR_MODULE = null;
    public static final Item DROPPER_MODULE = null;
    public static final Item EXTRUDER_MODULE_1 = null;
    public static final Item EXTRUDER_MODULE_2 = null;
    public static final Item FLINGER_MODULE = null;
    public static final Item FLUID_MODULE = null;
    public static final Item PLACER_MODULE = null;
    public static final Item PULLER_MODULE_1 = null;
    public static final Item PULLER_MODULE_2 = null;
    public static final Item SENDER_MODULE_1 = null;
    public static final Item SENDER_MODULE_2 = null;
    public static final Item SENDER_MODULE_3 = null;
    public static final Item VACUUM_MODULE = null;
    public static final Item VOID_MODULE = null;

    public static final Item BLAST_UPGRADE = null;
    public static final Item CAMOUFLAGE_UPGRADE = null;
    public static final Item FLUID_UPGRADE = null;
    public static final Item MUFFLER_UPGRADE = null;
    public static final Item SECURITY_UPGRADE = null;
    public static final Item SPEED_UPGRADE = null;
    public static final Item STACK_UPGRADE = null;
    public static final Item SYNC_UPGRADE = null;

    public static final Item FAST_PICKUP_AUGMENT = null;
    public static final Item MIMIC_AUGMENT = null;
    public static final Item PICKUP_DELAY_AUGMENT = null;
    public static final Item PUSHING_AUGMENT = null;
    public static final Item RANGE_DOWN_AUGMENT = null;
    public static final Item RANGE_UP_AUGMENT = null;
    public static final Item REDSTONE_AUGMENT = null;
    public static final Item REGULATOR_AUGMENT = null;
    public static final Item STACK_AUGMENT = null;
    public static final Item XP_VACUUM_AUGMENT = null;

    public static final Item BULK_ITEM_FILTER = null;
    public static final Item INSPECTION_FILTER = null;
    public static final Item MOD_FILTER = null;
    public static final Item REGEX_FILTER = null;

    private static final ItemGroup MR_CREATIVE_TAB = new ItemGroup(ModularRouters.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.ITEM_ROUTER);
        }
    };



    private static Item.Properties ib() {
        return new Item.Properties().group(MR_CREATIVE_TAB);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> r = event.getRegistry();

        // ItemBlocks
        register(r, new BlockItem(ModBlocks.ITEM_ROUTER, ib()), ModBlocks.ITEM_ROUTER.getRegistryName());
        register(r, new BlockItem(ModBlocks.TEMPLATE_FRAME, ib()), ModBlocks.TEMPLATE_FRAME.getRegistryName());

        // Misc items
        register(r, new Item(ib()),RL("blank_module"));
        register(r, new Item(ib()),RL("blank_upgrade"));
        register(r, new Item(ib()),RL("augment_core"));
        register(r, new Item(ib()),RL("override_card"));

        // Modules
        register(r, new ActivatorModule(ib()), RL("activator_module"));
        register(r, new BreakerModule(ib()), RL("breaker_module"));
        register(r, new DetectorModule(ib()), RL("detector_module"));
        register(r, new DistributorModule(ib()), RL("distributor_module"));
        register(r, new DropperModule(ib()), RL("dropper_module"));
        register(r, new ExtruderModule1(ib()), RL("extruder_module_1"));
        register(r, new ExtruderModule2(ib()), RL("extruder_module_2"));
        register(r, new FlingerModule(ib()), RL("flinger_module"));
        register(r, new FluidModule(ib()), RL("fluid_module"));
        register(r, new PlacerModule(ib()), RL("placer_module"));
        register(r, new PlayerModule(ib()), RL("player_module"));
        register(r, new PullerModule1(ib()), RL("puller_module_1"));
        register(r, new PullerModule2(ib()), RL("puller_module_2"));
        register(r, new SenderModule1(ib()), RL("sender_module_1"));
        register(r, new SenderModule2(ib()), RL("sender_module_2"));
        register(r, new SenderModule3(ib()), RL("sender_module_3"));
        register(r, new VacuumModule(ib()), RL("vacuum_module"));
        register(r, new VoidModule(ib()), RL("void_module"));

        // Upgrades
        register(r, new BlastUpgrade(ib()), RL("blast_upgrade"));
        register(r, new CamouflageUpgrade(ib()), RL("camouflage_upgrade"));
        register(r, new FluidUpgrade(ib()), RL("fluid_upgrade"));
        register(r, new MufflerUpgrade(ib()), RL("muffler_upgrade"));
        register(r, new SecurityUpgrade(ib()), RL("security_upgrade"));
        register(r, new SpeedUpgrade(ib()), RL("speed_upgrade"));
        register(r, new StackUpgrade(ib()), RL("stack_upgrade"));
        register(r, new SyncUpgrade(ib()), RL("sync_upgrade"));

        // Augments
        register(r, new FastPickupAugment(ib()), RL("fast_pickup_augment"));
        register(r, new MimicAugment(ib()), RL("mimic_augment"));
        register(r, new PickupDelayAugment(ib()), RL("pickup_delay_augment"));
        register(r, new PushingAugment(ib()), RL("pushing_augment"));
        register(r, new RangeAugments.RangeUpAugment(ib()), RL("range_up_augment"));
        register(r, new RangeAugments.RangeDownAugment(ib()), RL("range_down_augment"));
        register(r, new RedstoneAugment(ib()), RL("redstone_augment"));
        register(r, new RegulatorAugment(ib()), RL("regulator_augment"));
        register(r, new StackAugment(ib()), RL("stack_augment"));
        register(r, new XPVacuumAugment(ib()), RL("xp_vacuum_augment"));

        // Filters
        register(r, new BulkItemFilter(ib()), RL("bulk_item_filter"));
        register(r, new InspectionFilter(ib()), RL("inspection_filter"));
        register(r, new ModFilter(ib()), RL("mod_filter"));
        register(r, new RegexFilter(ib()), RL("regex_filter"));
    }

    private static void register(IForgeRegistry<Item> registry, Item item, ResourceLocation name) {
        registry.register(item.setRegistryName(name));
        All.items.add(item);
    }

    public interface ITintable {
        Color getItemTint();
    }

    public static class All {
        public static final List<Item> items = new ArrayList<>();
    }
}
