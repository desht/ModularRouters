package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.GuiModule;
import me.desht.modularrouters.gui.GuiModuleDetector;
import me.desht.modularrouters.logic.CompiledDetectorModuleSettings;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class DetectorModule extends Module {
    public enum SignalType {
        NONE, WEAK, STRONG;

        public static SignalType getType(boolean strong) {
            return strong ? STRONG : WEAK;
        }
    }

    @Override
    public boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings) {
        ItemStack stack = router.getBufferItemStack();

        if (stack == null || !settings.getFilter().pass(stack)) {
            return false;
        }

        CompiledDetectorModuleSettings dSettings = (CompiledDetectorModuleSettings) settings;
        router.emitRedstone(settings.getDirection(), dSettings.getSignalLevel(), SignalType.getType(dSettings.isStrongSignal()));

        return true;
    }

    @Override
    public CompiledModuleSettings compile(ItemStack stack) {
        return new CompiledDetectorModuleSettings(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        super.addExtraInformation(itemstack, player, list, par4);
        CompiledDetectorModuleSettings ds = new CompiledDetectorModuleSettings(itemstack);
        list.add(I18n.format("itemText.misc.redstoneLevel",
                ds.getSignalLevel(), I18n.format("itemText.misc.strongSignal" + ds.isStrongSignal())));
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModuleDetector.class;
    }
}
