package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule.MatchType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class BreakerModule extends ItemModule implements IPickaxeUser {
    private static final TintColor TINT_COLOR = new TintColor(240, 208, 208);

    public BreakerModule() {
        super(ModItems.defaultProps(), CompiledBreakerModule::new);
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    protected void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledBreakerModule cbm = new CompiledBreakerModule(null, itemstack);
        MatchType type = cbm.getMatchType();
        list.add(ClientUtil.xlate(type.getTranslationKey()).withStyle(TextFormatting.YELLOW));
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_BREAKER.get();
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return MRConfig.Common.EnergyCosts.breakerModuleEnergyCost;
    }
}
