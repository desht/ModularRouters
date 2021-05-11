package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ActivatorModule extends ItemModule {
    private static final TintColor TINT_COLOR = new TintColor(255, 255, 195);

    public ActivatorModule() {
        super(ModItems.defaultProps(), CompiledActivatorModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, List<ITextComponent> list) {
        super.addSettingsInformation(stack, list);

        CompiledActivatorModule cam = new CompiledActivatorModule(null, stack);
        list.add(ClientUtil.xlate("modularrouters.guiText.tooltip.activator.action").append(": ")
                .withStyle(TextFormatting.YELLOW)
                .append(ClientUtil.xlate("modularrouters.itemText.activator.action." + cam.getActionType())
                        .withStyle(TextFormatting.AQUA)));
        if (!cam.getActionType().isEntityTarget()) {
            list.add(ClientUtil.xlate("modularrouters.guiText.tooltip.activator.lookDirection").append(": ")
                    .withStyle(TextFormatting.YELLOW)
                    .append(ClientUtil.xlate("modularrouters.itemText.activator.direction." + cam.getLookDirection())
                            .withStyle(TextFormatting.AQUA)));
        } else {
            list.add(ClientUtil.xlate("modularrouters.guiText.tooltip.activator.entityMode").append(": ")
                    .withStyle(TextFormatting.YELLOW)
                    .append(ClientUtil.xlate("modularrouters.itemText.activator.entityMode." + cam.getEntityMode())
                            .withStyle(TextFormatting.AQUA)));
        }
        if (cam.isSneaking()) {
            list.add(ClientUtil.xlate("modularrouters.guiText.tooltip.activator.sneak").withStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        CompiledActivatorModule cam = new CompiledActivatorModule(null, stack);
        return cam.getActionType() == CompiledActivatorModule.ActionType.ATTACK_ENTITY ?
                MRConfig.Common.EnergyCosts.activatorModuleEnergyCostAttack :
                MRConfig.Common.EnergyCosts.activatorModuleEnergyCost;
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_ACTIVATOR.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }
}
