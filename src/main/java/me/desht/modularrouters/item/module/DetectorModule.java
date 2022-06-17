package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DetectorModule extends ModuleItem {

    private static final TintColor TINT_COLOR = new TintColor(255, 255, 195);

    public DetectorModule() {
        super(ModItems.defaultProps(), CompiledDetectorModule::new);
    }

    public enum SignalType {
        NONE, WEAK, STRONG;

        public static SignalType getType(boolean strong) {
            return strong ? STRONG : WEAK;
        }
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.DETECTOR_MENU.get();
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<Component> list) {
        super.addSettingsInformation(itemstack, list);
        CompiledDetectorModule ds = new CompiledDetectorModule(null, itemstack);
        list.add(ClientUtil.xlate("modularrouters.itemText.misc.redstoneLevel",
                ds.getSignalLevel(), I18n.get("modularrouters.itemText.misc.strongSignal." + ds.isStrongSignal())));
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.detectorModuleEnergyCost.get();
    }

    @Override
    public boolean isOmniDirectional() {
        return true;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

}
