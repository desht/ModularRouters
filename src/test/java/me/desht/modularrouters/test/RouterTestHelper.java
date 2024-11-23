package me.desht.modularrouters.test;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.ModuleTargetList;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import me.desht.modularrouters.logic.settings.ModuleSettings;
import me.desht.modularrouters.logic.settings.ModuleTermination;
import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class RouterTestHelper extends ExtendedGameTestHelper {
    public RouterTestHelper(GameTestInfo info) {
        super(info);
    }

    public RouterWrapper placeRouter(int x, int y, int z) {
        setBlock(x, y, z, ModBlocks.MODULAR_ROUTER.get());
        var router = getBlockEntity(x, y, z, ModularRouterBlockEntity.class);
        addEndListener(success -> {
            if (success) {
                router.setBufferItemStack(ItemStack.EMPTY);

                for (int i = 0; i < router.getModuleSlotCount(); i++) {
                    router.getModules().setStackInSlot(i, ItemStack.EMPTY);
                }

                for (int i = 0; i < router.getUpgradeSlotCount(); i++) {
                    ((IItemHandlerModifiable)router.getUpgrades()).setStackInSlot(i, ItemStack.EMPTY);
                }

                setBlock(x, y, z, Blocks.AIR);
            }
        });
        return new RouterWrapper(router, this);
    }

    public ChestBlockEntity placeChest(int x, int y, int z) {
        setBlock(x, y, z, Blocks.CHEST);
        return getBlockEntity(x, y, z, ChestBlockEntity.class);
    }

    public ItemEntity spawnItem(Item pItem, int count, Vec3 pPos) {
        ServerLevel serverlevel = this.getLevel();
        Vec3 vec3 = this.absoluteVec(pPos);
        ItemEntity itementity = new ItemEntity(serverlevel, vec3.x, vec3.y, vec3.z, new ItemStack(pItem, count));
        itementity.setDeltaMovement(0.0, 0.0, 0.0);
        serverlevel.addFreshEntity(itementity);
        return itementity;
    }

    public ItemEntity spawnItem(Item pItem, int count, float pX, float pY, float pZ) {
        return this.spawnItem(pItem, count, new Vec3(pX, pY, pZ));
    }

    public void assertStack(ItemStack stack, Item expectedItem, int expectedCount) {
        assertValueEqual(stack.getItem(), expectedItem, "stack item");
        assertValueEqual(stack.getCount(), expectedCount, "stack count");
    }

    public record RouterWrapper(ModularRouterBlockEntity router, RouterTestHelper helper) {
        public RouterWrapper maxSpeed() {
            addUpgrade(ModItems.SPEED_UPGRADE.toStack(9));
            return this;
        }

        public ItemStack addDirectionalModule(Supplier<? extends Item> module, RelativeDirection direction) {
            var stack = module.get().getDefaultInstance();
            stack.set(ModDataComponents.COMMON_MODULE_SETTINGS, new ModuleSettings(
                    ModuleFlags.DEFAULT,
                    direction,
                    ModuleTermination.NONE,
                    RedstoneBehaviour.ALWAYS,
                    0
            ));
            return addModule(stack);
        }

        public ItemStack addTargetedModule(Supplier<? extends Item> module, int x, int y, int z, Direction face) {
            var stack = module.get().getDefaultInstance();
            stack.set(ModDataComponents.MODULE_TARGET_LIST, new ModuleTargetList(
                    List.of(new ModuleTarget(helper.getLevel(), helper.absolutePos(new BlockPos(x, y, z)), face))
            ));
            return addModule(stack);
        }

        public ItemStack addModule(ItemStack module) {
            router.getModules().insertItem(0, module, false);
            return module;
        }

        public ItemStack addUpgrade(ItemStack module) {
            router.getUpgrades().insertItem(0, module, false);
            return module;
        }

        public ItemStack insertBuffer(ItemStack stack) {
            router.insertBuffer(stack);
            return stack;
        }

        public ItemStack setBuffer(ItemStack stack) {
            router.setBufferItemStack(stack);
            return stack;
        }

        public void modifyModuleSettings(int index, UnaryOperator<ModuleSettingsBuilder> mod) {
            modifyModule(index, s -> s.set(ModDataComponents.COMMON_MODULE_SETTINGS, mod.apply(new ModuleSettingsBuilder(s.get(ModDataComponents.COMMON_MODULE_SETTINGS))).build()));
        }

        public void modifyModule(int index, Consumer<ItemStack> mod) {
            var module = router.getModules().getStackInSlot(index).copy();
            mod.accept(module);
            router.getModules().setStackInSlot(index, module);
        }

        public void modifyAugments(int moduleIndex, Consumer<AugmentHandler> handler) {
            modifyModule(moduleIndex, stack -> handler.accept(new AugmentHandler(stack, router)));
        }

        public void clearBuffer() {
            router.extractBuffer(router.getBufferItemStack().getCount());
        }

        public ItemStack getBuffer() {
            return router.getBufferItemStack();
        }

        public void assertBuffer(Item item, int count) {
            var buf = router.getBufferItemStack();
            helper.assertValueEqual(buf.getItem(), item, "router buffer item");
            helper.assertValueEqual(buf.getCount(), count, "router buffer item count");
        }

        public void assertBufferEmpty() {
            helper.assertTrue(getBuffer().isEmpty(), "router buffer is not empty");
        }

        public int routerTicks(int routerTicks) {
            return (20 - IntStream.range(0, router().getUpgradeSlotCount())
                    .mapToObj(router().getUpgrades()::getStackInSlot)
                    .filter(s -> !s.isEmpty() && s.is(ModItems.SPEED_UPGRADE))
                    .mapToInt(ItemStack::getCount).sum() * 2) * routerTicks;
        }
    }

    public static class ModuleSettingsBuilder {
        private ModuleFlags flags;
        private RelativeDirection facing;
        private ModuleTermination termination;
        private RedstoneBehaviour redstoneBehaviour;
        private int regulatorAmount;

        public ModuleSettingsBuilder() {}
        public ModuleSettingsBuilder(ModuleSettings settings) {
            this.flags = settings.flags();
            this.facing = settings.facing();
            this.termination = settings.termination();
            this.redstoneBehaviour = settings.redstoneBehaviour();
            this.regulatorAmount = settings.regulatorAmount();
        }

        public ModuleSettingsBuilder facing(RelativeDirection direction) {
            this.facing = direction;
            return this;
        }

        public ModuleSettings build() {
            return new ModuleSettings(flags, facing, termination, redstoneBehaviour, regulatorAmount);
        }
    }
}
