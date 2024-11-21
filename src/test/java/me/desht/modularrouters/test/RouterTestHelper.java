package me.desht.modularrouters.test;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

import java.util.List;
import java.util.function.Supplier;

public class RouterTestHelper extends ExtendedGameTestHelper {
    public RouterTestHelper(GameTestInfo info) {
        super(info);
    }

    public ItemStack addDirectionalModule(ModularRouterBlockEntity router, Supplier<? extends Item> module, RelativeDirection direction) {
        var stack = module.get().getDefaultInstance();
        stack.set(ModDataComponents.COMMON_MODULE_SETTINGS, new ModuleSettings(
                ModuleFlags.DEFAULT,
                direction,
                ModuleTermination.NONE,
                RedstoneBehaviour.ALWAYS,
                0
        ));
        return addModule(router, stack);
    }

    public ItemStack addTargetedModule(ModularRouterBlockEntity router, Supplier<? extends Item> module, int x, int y, int z, Direction face) {
        var stack = module.get().getDefaultInstance();
        stack.set(ModDataComponents.MODULE_TARGET_LIST, new ModuleTargetList(
                List.of(new ModuleTarget(getLevel(), absolutePos(new BlockPos(x, y, z)), face))
        ));
        return addModule(router, stack);
    }

    public ItemStack addModule(ModularRouterBlockEntity router, ItemStack module) {
        router.getModules().insertItem(0, module, false);
        return module;
    }

    public ModularRouterBlockEntity placeRouter(int x, int y, int z) {
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
        return router;
    }

    public ChestBlockEntity placeChest(int x, int y, int z) {
        setBlock(x, y, z, Blocks.CHEST);
        return getBlockEntity(x, y, z, ChestBlockEntity.class);
    }
}
