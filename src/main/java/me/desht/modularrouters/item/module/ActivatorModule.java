package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.TintColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ActivatorModule extends ItemModule {
    public ActivatorModule(Properties props) {
        super(props);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, List<ITextComponent> list) {
        super.addSettingsInformation(stack, list);

        CompiledActivatorModule cam = new CompiledActivatorModule(null, stack);
        list.add(new StringTextComponent(
                TextFormatting.YELLOW + I18n.format("guiText.tooltip.activator.action") + ": "
                + TextFormatting.AQUA + I18n.format("itemText.activator.action." + cam.getActionType()))
        );
        if (cam.getActionType() != CompiledActivatorModule.ActionType.USE_ITEM_ON_ENTITY) {
            list.add(new StringTextComponent(
                    TextFormatting.YELLOW + I18n.format("guiText.tooltip.activator.lookDirection") + ": "
                    + TextFormatting.AQUA + I18n.format("itemText.activator.direction." + cam.getLookDirection()))
            );
        }
        if (cam.isSneaking()) {
            list.add(new StringTextComponent(
                    TextFormatting.YELLOW + I18n.format("guiText.tooltip.activator.sneak"))
            );
        }
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_ACTIVATOR;
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledActivatorModule(router, stack);
    }

    @Override
    public TintColor getItemTint() {
        return new TintColor(255, 255, 195);
    }
}
