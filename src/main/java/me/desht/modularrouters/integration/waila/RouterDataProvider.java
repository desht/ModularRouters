package me.desht.modularrouters.integration.waila;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;

public class RouterDataProvider /*implements IServerDataProvider<BlockEntity>*/ {
//    @Override
//    public void appendServerData(CompoundTag compoundNBT, ServerPlayer serverPlayerEntity, Level world, BlockEntity te) {
//        if (te instanceof ModularRouterBlockEntity) {
//            ModularRouterBlockEntity router = (ModularRouterBlockEntity) te;
//            if (router.isPermitted(serverPlayerEntity)) {
//                compoundNBT.putInt("ModuleCount", router.getModuleCount());
//                compoundNBT.putInt("RedstoneMode", router.getRedstoneBehaviour().ordinal());
//                compoundNBT.putBoolean("EcoMode", router.getEcoMode());
//                compoundNBT.put("Upgrades", getUpgrades(router));
//            } else {
//                compoundNBT.putBoolean("Denied", true);
//            }
//        }
//    }

    private CompoundTag getUpgrades(ModularRouterBlockEntity router) {
        IItemHandler handler = router.getUpgrades();
        Map<Item, Integer> counts = new HashMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() instanceof ItemUpgrade) {
                counts.put(stack.getItem(), counts.getOrDefault(stack.getItem(), 0) + stack.getCount());
            }
        }
        CompoundTag upgrades = new CompoundTag();
        counts.forEach((k, v) -> upgrades.putInt(k.getDescriptionId(), v));
        return upgrades;
    }
}
