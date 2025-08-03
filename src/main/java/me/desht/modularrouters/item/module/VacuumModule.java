package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.integration.XPCollection;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule.VacuumSettings;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModNameCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class VacuumModule extends ModuleItem implements IRangedModule {
    private static final TintColor TINT_COLOR = new TintColor(120, 48, 191);

    public VacuumModule(Properties properties) {
        super(properties.component(ModDataComponents.VACUUM_SETTINGS.get(), VacuumSettings.DEFAULT),
                CompiledVacuumModule::new);
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.VACUUM_MENU.get();
    }

    @Override
    public void addSettingsInformation(ItemStack stack, Consumer<Component> tooltipAdder) {
        super.addSettingsInformation(stack, tooltipAdder);

        boolean xpMode = new AugmentItem.AugmentCounter(stack).getAugmentCount(ModItems.XP_VACUUM_AUGMENT) > 0;

        if (xpMode) {
            VacuumSettings settings = stack.getOrDefault(ModDataComponents.VACUUM_SETTINGS, VacuumSettings.DEFAULT);
            XPCollection.XPCollectionType type = settings.collectionType();
            Component modName = Component.literal(ModNameCache.getModName(type.getModId())).withStyle(ChatFormatting.BLUE);
            Component title = type.getDisplayName().plainCopy().withStyle(ChatFormatting.AQUA);
            tooltipAdder.accept(ClientUtil.xlate("modularrouters.guiText.label.xpVacuum")
                    .append(": ").withStyle(ChatFormatting.YELLOW)
                    .append(title).append(" - ").append(modName));
            if (settings.autoEject() && !type.isSolid()) {
                tooltipAdder.accept(MiscUtil.settingsStr(ChatFormatting.GREEN.toString(), ClientUtil.xlate("modularrouters.guiText.tooltip.xpVacuum.ejectFluid")));
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
