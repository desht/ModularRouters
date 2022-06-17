package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class PlayerModule extends ModuleItem implements IPlayerOwned {

    private static final TintColor TINT_COLOR = new TintColor(255, 208, 144);

    public PlayerModule() {
        super(ModItems.defaultProps(), CompiledPlayerModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<Component> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledPlayerModule cpm = new CompiledPlayerModule(null, itemstack);
        String owner = cpm.getPlayerName() == null ? "-" : cpm.getPlayerName();
        list.add(MiscUtil.settingsStr(ChatFormatting.YELLOW.toString(),
                ClientUtil.xlate("modularrouters.itemText.security.owner", owner)));

        String s = String.format(ChatFormatting.YELLOW + "%s: " + ChatFormatting.AQUA + "%s %s %s",
                I18n.get("modularrouters.itemText.misc.operation"),
                I18n.get("block.modularrouters.modular_router"),
                cpm.getOperation().getSymbol(),
                I18n.get("modularrouters.guiText.label.playerSect." + cpm.getSection()));
        list.add(Component.literal(s));
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
}
