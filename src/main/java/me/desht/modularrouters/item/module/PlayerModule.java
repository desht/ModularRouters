package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule.PlayerSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.colorText;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class PlayerModule extends ModuleItem implements IPlayerOwned {
    private static final TintColor TINT_COLOR = new TintColor(255, 208, 144);

    public PlayerModule() {
        super(ModItems.moduleProps()
                        .component(ModDataComponents.PLAYER_SETTINGS, PlayerSettings.DEFAULT),
                CompiledPlayerModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack stack, List<Component> list) {
        super.addSettingsInformation(stack, list);

        PlayerSettings settings = stack.getOrDefault(ModDataComponents.PLAYER_SETTINGS, PlayerSettings.DEFAULT);
        ResolvableProfile profile = stack.get(ModDataComponents.OWNER);

        String owner = profile == null ? "-" : profile.gameProfile().getName();
        list.add(xlate("modularrouters.itemText.security.owner", colorText(owner, ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));

        Component c = xlate("modularrouters.itemText.misc.operation").withStyle(ChatFormatting.YELLOW)
                .append(": ")
                .append(xlate("block.modularrouters.modular_router")
                        .append(" ")
                        .append(settings.direction().getSymbol())
                        .append(" ")
                        .append(xlate(settings.section().getTranslationKey()))
                        .withStyle(ChatFormatting.AQUA)
                );
        list.add(c);
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.PLAYER_MENU.get();
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (ctx.getPlayer() != null && ctx.getPlayer().isSteppingCarefully()) {
            setOwner(ctx.getItemInHand(), ctx.getPlayer());
            ctx.getPlayer().displayClientMessage(Component.translatable("modularrouters.itemText.security.owner", ctx.getPlayer().getDisplayName()), false);
            return InteractionResult.SUCCESS;
        } else {
            return super.useOn(ctx);
        }
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.playerModuleEnergyCost.get();
    }

    @Override
    public void doModuleValidation(ItemStack stack, ServerPlayer player) {
        TargetValidation v = ModularRouters.getDimensionBlacklist().test(player.level().dimension().location()) ?
                TargetValidation.BAD_DIMENSION :
                TargetValidation.OK;
        MutableComponent msg = Component.translatable(v.translationKey()).withStyle(v.getColor());
        player.displayClientMessage(msg, false);
    }
}
