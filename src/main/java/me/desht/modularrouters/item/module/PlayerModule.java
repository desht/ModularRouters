package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class PlayerModule extends ItemModule implements IPlayerOwned {

    private static final TintColor TINT_COLOR = new TintColor(255, 208, 144);

    public PlayerModule() {
        super(ModItems.defaultProps(), CompiledPlayerModule::new);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledPlayerModule cpm = new CompiledPlayerModule(null, itemstack);
        String owner = cpm.getPlayerName() == null ? "-" : cpm.getPlayerName();
        list.add(MiscUtil.settingsStr(TextFormatting.YELLOW.toString(),
                ClientUtil.xlate("modularrouters.itemText.security.owner", owner)));

        String s = String.format(TextFormatting.YELLOW + "%s: " + TextFormatting.AQUA + "%s %s %s",
                I18n.get("modularrouters.itemText.misc.operation"),
                I18n.get("block.modularrouters.item_router"),
                cpm.getOperation().getSymbol(),
                I18n.get("modularrouters.guiText.label.playerSect." + cpm.getSection()));
        list.add(new StringTextComponent(s));
    }

    @Override
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_PLAYER.get();
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        if (ctx.getLevel().isClientSide) {
            return ActionResultType.SUCCESS;
        } else if (ctx.getPlayer() != null && ctx.getPlayer().isSteppingCarefully()) {
            setOwner(ctx.getItemInHand(), ctx.getPlayer());
            ctx.getPlayer().displayClientMessage(new TranslationTextComponent("modularrouters.itemText.security.owner", ctx.getPlayer().getDisplayName()), false);
            return ActionResultType.SUCCESS;
        } else {
            return super.useOn(ctx);
        }
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getEnergyCost() {
        return MRConfig.Common.EnergyCosts.playerModuleEnergyCost;
    }
}
