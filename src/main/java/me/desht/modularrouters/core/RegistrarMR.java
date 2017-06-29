package me.desht.modularrouters.core;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.BlockTemplateFrame;
import me.desht.modularrouters.client.TemplateFrameModel;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ItemSubTypes;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import static me.desht.modularrouters.util.MiscUtil.RL;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder(ModularRouters.MODID)
public class RegistrarMR {
    // Blocks
    @GameRegistry.ObjectHolder("item_router")
    public static final BlockItemRouter ITEM_ROUTER = null;
    @GameRegistry.ObjectHolder("template_frame")
    public static final BlockTemplateFrame TEMPLATE_FRAME = null;

    // ItemBlocks
    @GameRegistry.ObjectHolder("item_router")
    private static final ItemBlock ITEM_ROUTER_ITEM = null;
    @GameRegistry.ObjectHolder("template_frame")
    private static final ItemBlock TEMPLATE_FRAME_ITEM = null;

    // Items
    @GameRegistry.ObjectHolder("blank_module")
    public static final ItemBase BLANK_MODULE = null;
    @GameRegistry.ObjectHolder("blank_upgrade")
    public static final ItemBase BLANK_UPGRADE = null;
    @GameRegistry.ObjectHolder("override_card")
    public static final ItemBase OVERRIDE_CARD = null;
    @GameRegistry.ObjectHolder("augment_core")
    public static final ItemBase AUGMENT_CORE = null;
    @GameRegistry.ObjectHolder("module")
    public static final ItemModule MODULE = null;
    @GameRegistry.ObjectHolder("upgrade")
    public static final ItemUpgrade UPGRADE = null;
    @GameRegistry.ObjectHolder("filter")
    public static final ItemSmartFilter FILTER = null;
    @GameRegistry.ObjectHolder("augment")
    public static final ItemAugment AUGMENT = null;

    // Sounds
    @GameRegistry.ObjectHolder("error")
    public static final SoundEvent SOUND_ERROR = null;
    @GameRegistry.ObjectHolder("success")
    public static final SoundEvent SOUND_SUCCESS = null;
    @GameRegistry.ObjectHolder("thud")
    public static final SoundEvent SOUND_THUD = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockItemRouter());
        event.getRegistry().register(new BlockTemplateFrame());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        // ItemBlocks
        event.getRegistry().register(new ItemBlock(ITEM_ROUTER).setRegistryName(ITEM_ROUTER.getRegistryName()));
        event.getRegistry().register(new ItemBlock(TEMPLATE_FRAME).setRegistryName(TEMPLATE_FRAME.getRegistryName()));

        // Items
        event.getRegistry().register(new ItemBase("blank_module"));
        event.getRegistry().register(new ItemBase("blank_upgrade"));
        event.getRegistry().register(new ItemBase("augment_core"));
        event.getRegistry().register(new ItemBase("override_card"));
        event.getRegistry().register(new ItemModule());
        event.getRegistry().register(new ItemUpgrade());
        event.getRegistry().register(new ItemSmartFilter());
        event.getRegistry().register(new ItemAugment());
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(new SoundEvent(RL("error")).setRegistryName("error"));
        event.getRegistry().register(new SoundEvent(RL("success")).setRegistryName("success"));
        event.getRegistry().register(new SoundEvent(RL("thud")).setRegistryName("thud"));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerSubItemModels(ModelRegistryEvent event) {
        // the can_emit property has no effect on block rendering, so let's not create unnecessary variants
        ModelLoader.setCustomStateMapper(RegistrarMR.ITEM_ROUTER, new StateMap.Builder().ignore(BlockItemRouter.CAN_EMIT).build());

        StateMapperBase ignoreState = new StateMapperBase() {
            @Nonnull
            @Override
            protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState iBlockState) {
                return TemplateFrameModel.variantTag;
            }
        };
        ModelLoader.setCustomStateMapper(RegistrarMR.TEMPLATE_FRAME, ignoreState);

        registerSimpleModels(ITEM_ROUTER_ITEM, TEMPLATE_FRAME_ITEM);
        registerSimpleModels(BLANK_MODULE, BLANK_UPGRADE, AUGMENT_CORE, OVERRIDE_CARD);

        registerSubItemModels(MODULE, UPGRADE, FILTER);
        registerSubItemModels(AUGMENT);
    }

    private static void registerSimpleModels(Item... items) {
        for (Item item : items) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    private static void registerSubItemModels(ItemBase... items) {
        for (ItemBase item : items) {
            for (int i = 0; i < item.getSubTypes(); i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(RL(item.getSubTypeName(i)), "inventory"));
            }
        }
    }

    private static void registerSubItemModels(ItemSubTypes... items) {
        for (ItemSubTypes item : items) {
            for (int i = 0; i < item.getSubTypes(); i++) {
                ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(RL(item.getSubTypeName(i)), "inventory"));
            }
        }
    }
}
