package me.desht.modularrouters.gui;

import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ModuleGuiFactory {
    public static GuiModule createGui(EntityPlayer player, EnumHand hand) {
        return createGui(player, player.getHeldItem(hand), null, -1, hand);
    }

    public static GuiModule createGui(EntityPlayer player, ItemStack moduleStack, BlockPos routerPos, int slotIndex) {
        return createGui(player, moduleStack, routerPos, slotIndex, null);
    }

    private static GuiModule createGui(EntityPlayer player, ItemStack moduleStack, BlockPos routerPos, int slotIndex, EnumHand hand) {
        Module module = ItemModule.getModule(moduleStack);
        if (module == null) {
            return null;
        }
        Class<? extends GuiModule> clazz = module.getGuiHandler();
        try {
            Constructor<? extends GuiModule> ctor = clazz.getConstructor(ModuleContainer.class, BlockPos.class, Integer.class, EnumHand.class);
            return ctor.newInstance(new ModuleContainer(player, moduleStack), routerPos, slotIndex, hand);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
