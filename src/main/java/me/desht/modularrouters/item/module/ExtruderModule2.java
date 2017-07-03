package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerExtruder2Module;
import me.desht.modularrouters.container.ContainerExtruder2Module.TemplateHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleExtruder2;
import me.desht.modularrouters.logic.compiled.CompiledExtruder2Module;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

public class ExtruderModule2 extends Module implements IRangedModule {
    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledExtruder2Module(router, stack);
    }

    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);

        list.add(TextFormatting.YELLOW + I18n.format("itemText.extruder2.template"));
        TemplateHandler handler = new TemplateHandler(itemstack);
        int size = list.size();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack blockStack = handler.getStackInSlot(i);
            if (!blockStack.isEmpty()) {
                list.add(" \u2022 " + TextFormatting.AQUA + blockStack.getCount() + " x " + blockStack.getDisplayName());
            }
        }
        if (list.size() == size) {
            String s = list.get(size - 1);
            list.set(size - 1, s + " " + TextFormatting.AQUA + TextFormatting.ITALIC + I18n.format("itemText.misc.noItems"));
        }
    }

    @Override
    public ContainerModule createGuiContainer(EntityPlayer player, EnumHand hand, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerExtruder2Module(player, hand, moduleStack, router);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleExtruder2.class;
    }

    @Override
    public int getBaseRange() {
        return ConfigHandler.module.extruder2BaseRange;
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHandler.module.extruder2MaxRange;
    }

    @Override
    public Color getItemTint() {
        return new Color(227, 174, 27);
    }
}
