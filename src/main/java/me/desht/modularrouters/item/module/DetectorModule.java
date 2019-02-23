package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModuleDetector;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.awt.*;
import java.util.List;

public class DetectorModule extends ItemModule {
    public DetectorModule(Properties props) {
        super(props);
    }

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
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);
        CompiledDetectorModule ds = new CompiledDetectorModule(null, itemstack);
        list.add(new TextComponentTranslation("itemText.misc.redstoneLevel",
                ds.getSignalLevel(), I18n.format("itemText.misc.strongSignal." + ds.isStrongSignal())));
    }

    @Override
    public Class<? extends GuiModule> getGuiClass() {
        return GuiModuleDetector.class;
    }

    @Override
    public boolean isOmniDirectional() {
        return true;
    }

    @Override
    public Color getItemTint() {
        return new Color(255, 255, 195);
    }

}
