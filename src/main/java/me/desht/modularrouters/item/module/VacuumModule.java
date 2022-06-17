package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class VacuumModule extends ModuleItem implements IRangedModule {

    private static final TintColor TINT_COLOR = new TintColor(120, 48, 191);

    public VacuumModule() {
        super(ModItems.defaultProps(), CompiledVacuumModule::new);
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.VACUUM_MENU.get();
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<Component> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledVacuumModule cvm = new CompiledVacuumModule(null, itemstack);
        if (cvm.isXpMode()) {
            XPCollection.XPCollectionType type = cvm.getXPCollectionType();
            Component modName = Component.literal(ModNameCache.getModName(type.getModId())).withStyle(ChatFormatting.BLUE);
            Component title = type.getDisplayName().plainCopy().withStyle(ChatFormatting.AQUA);
            list.add(ClientUtil.xlate("modularrouters.guiText.label.xpVacuum")
                    .append(": ").withStyle(ChatFormatting.YELLOW)
                    .append(title).append(" - ").append(modName));
            if (cvm.isAutoEjecting() && !type.isSolid()) {
                list.add(MiscUtil.settingsStr(ChatFormatting.GREEN.toString(), ClientUtil.xlate("modularrouters.guiText.tooltip.xpVacuum.ejectFluid")));
            }
        }
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.vacuumBaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.vacuumMaxRange.get();
    }

    @Override
    public boolean isOmniDirectional() {
        return true;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.vacuumModuleEnergyCost.get();
    }
}
