package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class DetectorModule extends ItemModule {

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
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_DETECTOR.get();
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);
        CompiledDetectorModule ds = new CompiledDetectorModule(null, itemstack);
        list.add(ClientUtil.xlate("modularrouters.itemText.misc.redstoneLevel",
                ds.getSignalLevel(), I18n.format("modularrouters.itemText.misc.strongSignal." + ds.isStrongSignal())));
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
