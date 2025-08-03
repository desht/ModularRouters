package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.Extruder2ModuleMenu;
import me.desht.modularrouters.container.Extruder2ModuleMenu.TemplateHandler;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule2;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;
import static me.desht.modularrouters.util.MiscUtil.asMutableComponent;

public class ExtruderModule2 extends ModuleItem implements IRangedModule {
    private static final TintColor TINT_COLOR = new TintColor(227, 174, 27);

    public ExtruderModule2(Properties properties) {
        super(properties.component(ModDataComponents.EXTRUDER2_TEMPLATE, ItemContainerContents.EMPTY),
                CompiledExtruderModule2::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, Consumer<Component> tooltipAdder) {
        super.addSettingsInformation(stack, tooltipAdder);

        List<Component> list = new ArrayList<>();
        list.add(xlate("modularrouters.itemText.extruder2.template").withStyle(ChatFormatting.YELLOW));

        int size = list.size();
        TemplateHandler handler = new TemplateHandler(stack, null);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack blockStack = handler.getStackInSlot(i);
            if (!blockStack.isEmpty()) {
                list.add(Component.literal(" • " + ChatFormatting.AQUA + blockStack.getCount() + " x ").append(blockStack.getHoverName()));
            }
        }
        if (list.size() == size) {
            Component tc = list.get(size - 1);
            list.set(list.size() - 1, asMutableComponent(tc)
                    .append(" ")
                    .append(xlate("modularrouters.itemText.misc.noItems").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
            );
        }

        list.forEach(tooltipAdder);
    }

    @Override
    ModuleMenu createContainer(int windowId, Inventory invPlayer, MFLocator loc) {
        return new Extruder2ModuleMenu(windowId, invPlayer, loc);
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.extruder2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.extruder2MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.extruderModule2EnergyCost.get();
    }
}
