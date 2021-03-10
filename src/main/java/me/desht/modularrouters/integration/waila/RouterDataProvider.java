package me.desht.modularrouters.integration.waila;

import mcp.mobius.waila.api.IServerDataProvider;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;

public class RouterDataProvider implements IServerDataProvider<TileEntity> {
    @Override
    public void appendServerData(CompoundNBT compoundNBT, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
        if (te instanceof TileEntityItemRouter) {
            TileEntityItemRouter router = (TileEntityItemRouter) te;
            if (router.isPermitted(serverPlayerEntity)) {
                compoundNBT.putInt("ModuleCount", router.getModuleCount());
                compoundNBT.putInt("RedstoneMode", router.getRedstoneBehaviour().ordinal());
                compoundNBT.putBoolean("EcoMode", router.getEcoMode());
                compoundNBT.put("Upgrades", getUpgrades(router));
            } else {
                compoundNBT.putBoolean("Denied", true);
            }
        }
    }

    private CompoundNBT getUpgrades(TileEntityItemRouter router) {
        IItemHandler handler = router.getUpgrades();
        Map<Item, Integer> counts = new HashMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() instanceof ItemUpgrade) {
                counts.put(stack.getItem(), counts.getOrDefault(stack.getItem(), 0) + stack.getCount());
            }
        }
        CompoundNBT upgrades = new CompoundNBT();
        counts.forEach((k, v) -> upgrades.putInt(k.getDescriptionId(), v));
        return upgrades;
    }
}
