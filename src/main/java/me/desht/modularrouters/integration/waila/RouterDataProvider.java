package me.desht.modularrouters.integration.waila;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.HashMap;
import java.util.Map;

import static me.desht.modularrouters.util.MiscUtil.RL;

public class RouterDataProvider implements IServerDataProvider<BlockAccessor> {
    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ModularRouterBlockEntity router) {
            if (router.isPermitted(accessor.getPlayer())) {
                compoundTag.putInt("ModuleCount", router.getModuleCount());
                compoundTag.putInt("RedstoneMode", router.getRedstoneBehaviour().ordinal());
                compoundTag.putBoolean("EcoMode", router.getEcoMode());
                compoundTag.put("Upgrades", getUpgrades(router));
            } else {
                compoundTag.putBoolean("Denied", true);
            }
        }
    }

    private CompoundTag getUpgrades(ModularRouterBlockEntity router) {
        IItemHandler handler = router.getUpgrades();
        Map<Item, Integer> counts = new HashMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() instanceof UpgradeItem) {
                counts.put(stack.getItem(), counts.getOrDefault(stack.getItem(), 0) + stack.getCount());
            }
        }
        CompoundTag upgrades = new CompoundTag();
        counts.forEach((k, v) -> upgrades.putInt(k.getDescriptionId(), v));
        return upgrades;
    }

    @Override
    public ResourceLocation getUid() {
        return RL("router");
    }
}
