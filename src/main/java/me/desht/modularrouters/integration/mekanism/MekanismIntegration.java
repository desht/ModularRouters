package me.desht.modularrouters.integration.mekanism;

import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.container.handler.BufferHandler;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;

import javax.annotation.Nullable;
public final class MekanismIntegration {
    public static final BlockCapability<IChemicalHandler, net.minecraft.core.Direction> BLOCK_CHEMICAL =
            BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);
    public static final ItemCapability<IChemicalHandler, Void> ITEM_CHEMICAL =
            ItemCapability.createVoid(ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);

    public static final DeferredItem<Item> CHEMICAL_MODULE = ModItems.ITEMS.register("chemical_module", ChemicalModule1::new);
    public static final DeferredItem<Item> CHEMICAL_MODULE_2 = ModItems.ITEMS.register("chemical_module_2", ChemicalModule2::new);

    public static void register(IEventBus modBus) {
        modBus.addListener(MekanismIntegration::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(BLOCK_CHEMICAL, ModBlockEntities.MODULAR_ROUTER.get(),
                (router, side) -> {
                    ((BufferHandler) router.getBuffer()).getCapability(ITEM_CHEMICAL);
                    return new RouterChemicalHandler(router);
                });
    }

    @Nullable
    public static IChemicalHandler getItemHandler(ItemStack stack) {
        return stack.getCount() == 1 ? stack.getCapability(ITEM_CHEMICAL) : null;
    }

    private MekanismIntegration() {
    }
}
