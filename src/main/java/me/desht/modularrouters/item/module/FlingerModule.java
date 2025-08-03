package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule.FlingerSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static me.desht.modularrouters.client.util.ClientUtil.colorText;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class FlingerModule extends DropperModule {
    public static final float MIN_SPEED = 0.0f;
    public static final float MAX_SPEED = 20.0f;
    public static final float MIN_PITCH = -90.0f;
    public static final float MAX_PITCH = 90.0f;
    public static final float MIN_YAW = -60.0f;
    public static final float MAX_YAW = 60.0f;
    private static final TintColor TINT_COLOR = new TintColor(230, 204, 240);

    public FlingerModule(Properties properties) {
        super(properties.component(ModDataComponents.FLINGER_SETTINGS, FlingerSettings.DEFAULT),
                CompiledFlingerModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, Consumer<Component> tooltipAdder) {
        super.addSettingsInformation(stack, tooltipAdder);

        FlingerSettings settings = stack.getOrDefault(ModDataComponents.FLINGER_SETTINGS, FlingerSettings.DEFAULT);
        tooltipAdder.accept(xlate("modularrouters.itemText.misc.flingerDetails",
                colorText(settings.speed(), ChatFormatting.AQUA),
                colorText(settings.pitch(), ChatFormatting.AQUA),
                colorText(settings.yaw(), ChatFormatting.AQUA)
        ).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.FLINGER_MENU.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.flingerModuleEnergyCost.get();
    }
}
