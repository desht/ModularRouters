package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModuleDetector;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

public class DetectorModule extends Module {
    public enum SignalType {
        NONE, WEAK, STRONG;

        public static SignalType getType(boolean strong) {
            return strong ? STRONG : WEAK;
        }
    }


    @Override
    public CompiledModule compile(TileEntityItemRouter tileEntityItemRouter, ItemStack stack) {
        return new CompiledDetectorModule(tileEntityItemRouter, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        CompiledDetectorModule ds = new CompiledDetectorModule(null, itemstack);
        list.add(I18n.format("itemText.misc.redstoneLevel",
                ds.getSignalLevel(), I18n.format("itemText.misc.strongSignal." + ds.isStrongSignal())));
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleDetector.class;
    }

    @Override
    public Color getItemTint() {
        return new Color(255, 255, 195);
    }
}
