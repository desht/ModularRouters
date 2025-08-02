package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActivatorSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ActivatorModule extends ModuleItem implements IRangedModule {
    private static final TintColor TINT_COLOR = new TintColor(255, 255, 195);

    public ActivatorModule(Properties properties) {
        super(properties.component(ModDataComponents.ACTIVATOR_SETTINGS, ActivatorSettings.DEFAULT), CompiledActivatorModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, Consumer<Component> tooltipAdder) {
        super.addSettingsInformation(stack, tooltipAdder);

        ActivatorSettings settings = stack.getOrDefault(ModDataComponents.ACTIVATOR_SETTINGS, ActivatorSettings.DEFAULT);
        tooltipAdder.accept(xlate("modularrouters.guiText.tooltip.activator.action").append(": ")
                .withStyle(ChatFormatting.YELLOW)
                .append(xlate(settings.actionType().getTranslationKey()).withStyle(ChatFormatting.AQUA)));
        if (!settings.actionType().isEntityTarget()) {
            tooltipAdder.accept(xlate("modularrouters.guiText.tooltip.activator.lookDirection").append(": ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(xlate(settings.lookDirection().getTranslationKey()).withStyle(ChatFormatting.AQUA)));
        } else {
            tooltipAdder.accept(xlate("modularrouters.guiText.tooltip.activator.entityMode").append(": ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(xlate(settings.entityMode().getTranslationKey()).withStyle(ChatFormatting.AQUA)));
        }
        if (settings.sneaking()) {
            tooltipAdder.accept(xlate("modularrouters.guiText.tooltip.activator.sneak").withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        ActivatorSettings settings = stack.get(ModDataComponents.ACTIVATOR_SETTINGS);
        return settings.actionType() == CompiledActivatorModule.ActionType.ATTACK_ENTITY ?
                ConfigHolder.common.energyCosts.activatorModuleEnergyCostAttack.get() :
                ConfigHolder.common.energyCosts.activatorModuleEnergyCost.get();
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.ACTIVATOR_MENU.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getBaseRange() {
        return 5;
    }

    @Override
    public int getHardMaxRange() {
        return 10;
    }
}
