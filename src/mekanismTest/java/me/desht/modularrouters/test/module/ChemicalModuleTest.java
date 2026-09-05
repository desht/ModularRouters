package me.desht.modularrouters.test.module;

import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.mekanism.ChemicalTransfer;
import me.desht.modularrouters.integration.mekanism.MekanismIntegration;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule.FluidModuleSettings;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
import me.desht.modularrouters.logic.settings.TransferDirection;
import me.desht.modularrouters.test.RouterTestHelper;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.List;
import java.util.Objects;

public class ChemicalModuleTest {
    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalMekanismTankSidedAccess(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        helper.setBlock(1, 2, 1, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("mekanism", "basic_chemical_tank")));
        var tank = helper.getBlockEntity(1, 2, 1, TileEntityChemicalTank.class);
        tank.getChemicalTank().setStack(chemical("hydrogen", 5000));
        var config = tank.configComponent.getConfig(TransmissionType.CHEMICAL);
        config.setDataType(DataType.INPUT, RelativeSide.TOP);
        config.setDataType(DataType.OUTPUT, RelativeSide.BOTTOM);
        router.setBuffer(tank("hydrogen", 0));
        router.addTargetedModule(MekanismIntegration.CHEMICAL_MODULE_2, 1, 2, 1, Direction.UP);
        configure(router, TransferDirection.TO_ROUTER, 100);

        helper.startSequence().thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 0L, "input face cannot be extracted");
            router.router().getModules().setStackInSlot(0, ItemStack.EMPTY);
            router.addTargetedModule(MekanismIntegration.CHEMICAL_MODULE_2, 1, 2, 1, Direction.DOWN);
            configure(router, TransferDirection.TO_ROUTER, 100);
        }).thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 100L, "output face permits extraction");
            helper.assertValueEqual(tank.getChemicalTank().getStored(), 4900L, "Mekanism tank contents conserved");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalModulesShareAllowance(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var source = helper.placeRouter(1, 2, 1);
        router.setBuffer(tank("hydrogen", 0));
        source.setBuffer(tank("hydrogen", 5000));
        router.addDirectionalModule(MekanismIntegration.CHEMICAL_MODULE, RelativeDirection.UP);
        router.addDirectionalModule(MekanismIntegration.CHEMICAL_MODULE, RelativeDirection.UP);

        helper.startSequence().thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 1000L, "shared allowance across both modules");
            helper.assertValueEqual(amount(source.getBuffer()), 4000L, "source conserved");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalRedstoneAndRadiationRestrictions(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var source = helper.placeRouter(1, 2, 1);
        router.setBuffer(tank("hydrogen", 0));
        source.setBuffer(tank("hydrogen", 5000));
        router.addDirectionalModule(MekanismIntegration.CHEMICAL_MODULE, RelativeDirection.UP);
        configure(router, TransferDirection.TO_ROUTER, 100);
        router.modifyAugments(0, aug -> aug.insertItem(0, ModItems.REDSTONE_AUGMENT.toStack(), false));
        router.modifyModuleSettings(0, b -> b.redstone(RedstoneBehaviour.NEVER));

        helper.startSequence().thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 0L, "redstone disables transfer");
            router.modifyModuleSettings(0, b -> b.redstone(RedstoneBehaviour.ALWAYS));
        }).thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 100L, "redstone enables transfer");
            ItemStack empty = tank("hydrogen", 0);
            ChemicalStack waste = chemical("nuclear_waste", 100);
            helper.assertValueEqual(handler(empty).insertChemical(waste, Action.SIMULATE).getAmount(), 100L,
                    "ordinary item tank still rejects radioactive waste");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalPercentRegulation(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var target = helper.placeRouter(1, 2, 1);
        router.setBuffer(tank("hydrogen", 5000));
        ItemStack destination = tank("hydrogen", 0);
        long threshold = handler(destination).getChemicalTankCapacity(0) / 2;
        handler(destination).setChemicalInTank(0, chemical("hydrogen", threshold - 19));
        target.setBuffer(destination);
        router.addDirectionalModule(MekanismIntegration.CHEMICAL_MODULE, RelativeDirection.UP);
        configure(router, TransferDirection.FROM_ROUTER, 1000);
        router.modifyAugments(0, aug -> aug.insertItem(0, ModItems.REGULATOR_AUGMENT.toStack(), false));
        router.modifyModuleSettings(0, b -> b.regulated(50));

        helper.startSequence().thenIdle(router.routerTicks(2)).thenExecute(() -> {
            helper.assertValueEqual(amount(target.getBuffer()), threshold, "percent regulator stops at exactly half capacity");
            helper.assertValueEqual(amount(router.getBuffer()), 4981L, "percent regulator conserves buffer");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalPullMk1(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var source = helper.placeRouter(1, 2, 1);
        router.setBuffer(tank("hydrogen", 0));
        source.setBuffer(tank("hydrogen", 5000));
        router.addDirectionalModule(MekanismIntegration.CHEMICAL_MODULE, RelativeDirection.UP);
        configure(router, TransferDirection.TO_ROUTER, 200);

        helper.startSequence().thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 200L, "pulled amount");
            helper.assertValueEqual(amount(source.getBuffer()), 4800L, "remaining source");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate("5x5x5")
    static void chemicalPushMk2(final RouterTestHelper helper) {
        var router = helper.placeRouter(3, 3, 3);
        var target = helper.placeRouter(0, 1, 0);
        router.setBuffer(tank("oxygen", 5000));
        target.setBuffer(tank("oxygen", 0));
        router.addTargetedModule(MekanismIntegration.CHEMICAL_MODULE_2, 0, 1, 0, Direction.UP);
        configure(router, TransferDirection.FROM_ROUTER, 300);

        helper.startSequence().thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(target.getBuffer()), 300L, "pushed amount");
            helper.assertValueEqual(amount(router.getBuffer()), 4700L, "remaining buffer");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalPartialTransferAndRejection(final RouterTestHelper helper) {
        ItemStack source = tank("hydrogen", 1000);
        ItemStack destination = tank("hydrogen", 0);
        IChemicalHandler from = handler(source);
        IChemicalHandler to = handler(destination);
        long capacity = to.getChemicalTankCapacity(0);
        to.setChemicalInTank(0, chemical("hydrogen", capacity - 37));
        helper.assertValueEqual(ChemicalTransfer.transfer(from, to, 500, s -> true), 37L, "partial fill");
        helper.assertValueEqual(amount(source), 963L, "source conserved");
        helper.assertValueEqual(amount(destination), capacity, "destination full");
        helper.assertValueEqual(ChemicalTransfer.transfer(from, to, 500, s -> true), 0L, "full destination");
        to.setChemicalInTank(0, chemical("oxygen", 1));
        helper.assertValueEqual(ChemicalTransfer.transfer(from, to, 500, s -> true), 0L, "different chemical rejected");
        helper.assertValueEqual(amount(source), 963L, "rejection preserves source");
        helper.assertValueEqual(ChemicalTransfer.transfer(from, from, 500, s -> true), 0L, "self transfer");
        helper.assertValueEqual(ChemicalTransfer.transfer(from, to, 0, s -> true), 0L, "zero allowance");
        helper.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalProxyFollowsBufferAndPersists(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        IChemicalHandler proxy = Objects.requireNonNull(helper.getLevel().getCapability(MekanismIntegration.BLOCK_CHEMICAL,
                router.router().getBlockPos(), Direction.UP));
        helper.assertValueEqual(proxy.getChemicalTanks(), 0, "empty buffer");
        ItemStack oldTank = tank("hydrogen", 250);
        router.setBuffer(oldTank);
        helper.assertValueEqual(proxy.extractChemical(70, Action.SIMULATE).getAmount(), 70L, "simulated extract");
        helper.assertValueEqual(amount(oldTank), 250L, "simulation preserves contents");
        proxy.extractChemical(70, Action.EXECUTE);
        helper.assertValueEqual(amount(oldTank), 180L, "executed extract");
        ItemStack replacement = tank("oxygen", 0);
        router.setBuffer(replacement);
        proxy.insertChemical(chemical("oxygen", 50), Action.EXECUTE);
        helper.assertValueEqual(amount(oldTank), 180L, "old capability not mutated");
        helper.assertValueEqual(amount(replacement), 50L, "replacement updated");

        var saved = replacement.save(helper.getLevel().registryAccess());
        router.setBuffer(ItemStack.parse(helper.getLevel().registryAccess(), saved).orElseThrow());
        helper.assertValueEqual(proxy.getChemicalInTank(0).getAmount(), 50L, "saved container components restored");
        ItemStack stacked = replacement.copyWithCount(2);
        router.setBuffer(stacked);
        helper.assertValueEqual(proxy.getChemicalTanks(), 0, "stacked containers rejected");
        router.setBuffer(new ItemStack(Items.STONE));
        helper.assertValueEqual(proxy.insertChemical(chemical("oxygen", 50), Action.EXECUTE).getAmount(), 50L, "non-container rejects input");
        helper.succeed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalFilterWhitelistAndBlacklist(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var source = helper.placeRouter(1, 2, 1);
        router.setBuffer(tank("hydrogen", 0));
        source.setBuffer(tank("hydrogen", 5000));
        router.addDirectionalModule(MekanismIntegration.CHEMICAL_MODULE, RelativeDirection.UP);
        configure(router, TransferDirection.TO_ROUTER, 100);
        router.modifyModule(0, stack -> stack.set(ModDataComponents.FILTER,
                ItemContainerContents.fromItems(List.of(tank("oxygen", 1)))));
        router.modifyModuleSettings(0, b -> b.flags(new ModuleFlags(true, false, false, false, false)));

        helper.startSequence().thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 0L, "whitelist rejects hydrogen");
            router.modifyModuleSettings(0, b -> b.flags(ModuleFlags.DEFAULT));
        }).thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 100L, "blacklist permits hydrogen");
            router.modifyModule(0, stack -> stack.set(ModDataComponents.FILTER,
                    ItemContainerContents.fromItems(List.of(tank("hydrogen", 1)))));
        }).thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 100L, "blacklist rejects hydrogen");
            helper.assertValueEqual(amount(source.getBuffer()), 4900L, "filtered source conserved");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate
    static void chemicalRegulatorDoesNotOvershoot(final RouterTestHelper helper) {
        var router = helper.placeRouter(1, 1, 1);
        var target = helper.placeRouter(1, 2, 1);
        router.setBuffer(tank("hydrogen", 5000));
        target.setBuffer(tank("hydrogen", 200));
        router.addDirectionalModule(MekanismIntegration.CHEMICAL_MODULE, RelativeDirection.UP);
        router.modifyModule(0, s -> s.set(ModDataComponents.FLUID_SETTINGS,
                new FluidModuleSettings(1000, TransferDirection.FROM_ROUTER, false, true)));
        router.modifyAugments(0, aug -> aug.insertItem(0, ModItems.REGULATOR_AUGMENT.toStack(), false));
        router.modifyModuleSettings(0, b -> b.regulated(250));

        helper.startSequence().thenIdle(router.routerTicks(2)).thenExecute(() -> {
            helper.assertValueEqual(amount(target.getBuffer()), 250L, "regulator stops at target");
            helper.assertValueEqual(amount(router.getBuffer()), 4950L, "only missing amount extracted");
            router.modifyModule(0, s -> s.set(ModDataComponents.FLUID_SETTINGS,
                    new FluidModuleSettings(1000, TransferDirection.TO_ROUTER, false, true)));
            router.modifyModuleSettings(0, b -> b.regulated(225));
        }).thenIdle(router.routerTicks(2)).thenExecute(() -> {
            helper.assertValueEqual(amount(target.getBuffer()), 225L, "pull leaves regulated amount");
            helper.assertValueEqual(amount(router.getBuffer()), 4975L, "regulated pull conserved");
        }).thenSucceed();
    }

    @GameTest
    @TestHolder
    @EmptyTemplate("5x5x5")
    static void chemicalRangeAndRemovedTarget(final RouterTestHelper helper) {
        var router = helper.placeRouter(3, 3, 3);
        var target = helper.placeRouter(0, 1, 0);
        router.setBuffer(tank("hydrogen", 1000));
        target.setBuffer(tank("hydrogen", 0));
        router.addTargetedModule(MekanismIntegration.CHEMICAL_MODULE_2, 0, 1, 0, Direction.UP);
        configure(router, TransferDirection.FROM_ROUTER, 100);
        router.modifyAugments(0, aug -> aug.insertItem(0, ModItems.RANGE_DOWN_AUGMENT.toStack(22), false));

        helper.startSequence().thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(target.getBuffer()), 0L, "out of range");
            router.modifyAugments(0, aug -> aug.extractItem(0, 64, false));
        }).thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(target.getBuffer()), 100L, "restored range");
            target.setBuffer(ItemStack.EMPTY);
        }).thenIdle(router.routerTicks(1)).thenExecute(() -> {
            helper.assertValueEqual(amount(router.getBuffer()), 900L, "removed container preserves source");
        }).thenSucceed();
    }

    private static void configure(RouterTestHelper.RouterWrapper router, TransferDirection direction, int limit) {
        router.modifyModule(0, stack -> stack.set(ModDataComponents.FLUID_SETTINGS,
                new FluidModuleSettings(limit, direction, false, false)));
    }

    private static ItemStack tank(String chemical, long amount) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "basic_chemical_tank")));
        if (amount > 0) handler(stack).setChemicalInTank(0, chemical(chemical, amount));
        return stack;
    }

    private static ChemicalStack chemical(String name, long amount) {
        return new ChemicalStack(MekanismAPI.CHEMICAL_REGISTRY.getHolder(ResourceLocation.fromNamespaceAndPath("mekanism", name)).orElseThrow(), amount);
    }

    private static IChemicalHandler handler(ItemStack stack) {
        return Objects.requireNonNull(MekanismIntegration.getItemHandler(stack), "Mekanism chemical tank capability");
    }

    private static long amount(ItemStack stack) {
        return handler(stack).getChemicalInTank(0).getAmount();
    }
}
