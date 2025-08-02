package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActivatorSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ActivatorModule extends ModuleItem implements IRangedModule {
    private static final TintColor TINT_COLOR = new TintColor(255, 255, 195);

    public ActivatorModule() {
        super(ModItems.moduleProps().component(ModDataComponents.ACTIVATOR_SETTINGS, ActivatorSettings.DEFAULT), CompiledActivatorModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, List<Component> list) {
        super.addSettingsInformation(stack, list);

        ActivatorSettings settings = stack.getOrDefault(ModDataComponents.ACTIVATOR_SETTINGS, ActivatorSettings.DEFAULT);
        list.add(xlate("modularrouters.guiText.tooltip.activator.action").append(": ")
                .withStyle(ChatFormatting.YELLOW)
                .append(xlate(settings.actionType().getTranslationKey()).withStyle(ChatFormatting.AQUA)));
        if (!settings.actionType().isEntityTarget()) {
            list.add(xlate("modularrouters.guiText.tooltip.activator.lookDirection").append(": ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(xlate(settings.lookDirection().getTranslationKey()).withStyle(ChatFormatting.AQUA)));
        } else {
            list.add(xlate("modularrouters.guiText.tooltip.activator.entityMode").append(": ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(xlate(settings.entityMode().getTranslationKey()).withStyle(ChatFormatting.AQUA)));
        }
        if (settings.sneaking()) {
            list.add(xlate("modularrouters.guiText.tooltip.activator.sneak").withStyle(ChatFormatting.YELLOW));
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
        return 0;
    }

    @Override
    public int getHardMaxRange() {
        return 4;
    }
}
