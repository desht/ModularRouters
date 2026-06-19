package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.render.IPositionProvider;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.logic.compiled.CompiledEnergyDistributorModule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.function.Consumer;

import static me.desht.modularrouters.logic.compiled.CompiledDistributorModule.DistributorSettings;

public class EnergyDistributorModule extends ModuleItem implements IRangedModule, IPositionProvider, ITargetedModule {
    private static final TintColor TINT_COLOR = new TintColor(79, 9, 90);

    public EnergyDistributorModule(Properties properties) {
        super(properties.component(ModDataComponents.DISTRIBUTOR_SETTINGS, DistributorSettings.DEFAULT), CompiledEnergyDistributorModule::new);
    }

    @Override
    protected void addSettingsInformation(ItemStack stack, Consumer<Component> list) {
        super.addSettingsInformation(stack, list);

        DistributorSettings settings = stack.getOrDefault(ModDataComponents.DISTRIBUTOR_SETTINGS, DistributorSettings.DEFAULT);
        list.accept(ClientUtil.xlate(settings.direction().getTranslationKey()).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.ENERGY_DISTRIBUTOR_MENU.get();
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.energydistributorModuleEnergyCost.get();
    }

    @Override
    public int getBaseRange() {
        return 8;
    }

    @Override
    public int getHardMaxRange() {
        return 48;
    }

    @Override
    public boolean isValidTarget(UseOnContext ctx) {
        return ctx.getLevel().getCapability(Capabilities.Energy.BLOCK, ctx.getClickedPos(), ctx.getClickedFace()) != null;
    }

    @Override
    public int getMaxTargets() {
        return 8;
    }

    @Override
    public int getRenderColor(int index) {
        return 0x80E08080;
    }
}
