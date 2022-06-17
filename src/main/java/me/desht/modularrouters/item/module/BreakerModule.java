package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BreakerModule extends ModuleItem implements IPickaxeUser {
    private static final TintColor TINT_COLOR = new TintColor(240, 208, 208);

    public BreakerModule() {
        super(ModItems.defaultProps(), CompiledBreakerModule::new);
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.breakerModuleEnergyCost.get();
    }

    @Override
    protected void addSettingsInformation(ItemStack itemstack, List<Component> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledBreakerModule cbm = new CompiledBreakerModule(null, itemstack);
        CompiledBreakerModule.MatchType type = cbm.getMatchType();
        list.add(ClientUtil.xlate(type.getTranslationKey()).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.BREAKER_MENU.get();
    }
}
