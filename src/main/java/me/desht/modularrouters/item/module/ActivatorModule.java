package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class ActivatorModule extends ItemModule {
    public ActivatorModule() {
        super(ModItems.defaultProps());
    }

    @Override
    public void addSettingsInformation(ItemStack stack, List<ITextComponent> list) {
        super.addSettingsInformation(stack, list);

        CompiledActivatorModule cam = new CompiledActivatorModule(null, stack);
        list.add(ClientUtil.xlate("guiText.tooltip.activator.action").appendString(": ")
                .mergeStyle(TextFormatting.YELLOW)
                .append(ClientUtil.xlate("itemText.activator.action." + cam.getActionType())
                        .mergeStyle(TextFormatting.AQUA)));
        if (cam.getActionType() != CompiledActivatorModule.ActionType.USE_ITEM_ON_ENTITY) {
            list.add(ClientUtil.xlate("guiText.tooltip.activator.lookDirection").appendString(": ")
                    .mergeStyle(TextFormatting.YELLOW)
                    .append(ClientUtil.xlate("itemText.activator.direction." + cam.getLookDirection())
                            .mergeStyle(TextFormatting.AQUA)));
        } else {
            list.add(ClientUtil.xlate("guiText.tooltip.activator.entityMode").appendString(": ")
                    .mergeStyle(TextFormatting.YELLOW)
                    .append(ClientUtil.xlate("itemText.activator.entityMode." + cam.getEntityMode())
                            .mergeStyle(TextFormatting.AQUA)));
        }
        if (cam.isSneaking()) {
            list.add(ClientUtil.xlate("guiText.tooltip.activator.sneak").mergeStyle(TextFormatting.YELLOW));
        }
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_ACTIVATOR.get();
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
