package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule1;
import me.desht.modularrouters.logic.settings.ModuleSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.function.Consumer;

public class ExtruderModule1 extends ModuleItem implements IRangedModule, IPickaxeUser {

    private static final TintColor TINT_COLOR = new TintColor(227, 174, 27);

    public ExtruderModule1(Properties properties) {
        super(properties.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY),
                CompiledExtruderModule1::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, Consumer<Component> list) {
        super.addSettingsInformation(stack, list);

        ModuleSettings settings = ModuleItem.getCommonSettings(stack);
        list.accept(ClientUtil.xlate("modularrouters.itemText.extruder.mode." + settings.redstoneBehaviour().getSerializedName())
                .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.extruderModule1EnergyCost.get();
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.extruder1BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.extruder1MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }
}
