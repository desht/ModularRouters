package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.module.GuiModuleActivator;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.xml.soap.Text;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ActivatorModule extends Module {
    @Override
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        CompiledActivatorModule cam = new CompiledActivatorModule(null, itemstack);
        list.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip.activator.action") + ": "
                + TextFormatting.AQUA + I18n.format("itemText.activator.action." + cam.getActionType()));
        list.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip.activator.sneak") + ": "
                + (cam.isSneaking() ? TextFormatting.GREEN  + "\u2714" : TextFormatting.RED + "\u2718"));
        if (cam.getActionType() != CompiledActivatorModule.ActionType.USE_ITEM_ON_ENTITY) {
            list.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip.activator.lookDirection") + ": "
                    + TextFormatting.AQUA + I18n.format("itemText.activator.direction." + cam.getLookDirection()));
        }
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledActivatorModule(router, stack);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleActivator.class;
    }

    @Override
    public Color getItemTint() {
        return new Color(255, 255, 195);
    }
}
