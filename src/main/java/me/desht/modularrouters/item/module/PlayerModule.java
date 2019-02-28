package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.client.gui.module.GuiModulePlayer;
import me.desht.modularrouters.item.IPlayerOwned;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.List;

public class PlayerModule extends ItemModule implements IPlayerOwned {
    public PlayerModule(Properties props) {
        super(props);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        super.addSettingsInformation(itemstack, list);

        CompiledPlayerModule cpm = new CompiledPlayerModule(null, itemstack);
        list.add(MiscUtil.settingsStr(TextFormatting.YELLOW.toString(),
                new TextComponentTranslation("itemText.security.owner", cpm.getPlayerName())));

        String s = String.format(TextFormatting.YELLOW + "%s: " + TextFormatting.AQUA + "%s %s %s",
                I18n.format("itemText.misc.operation"),
                I18n.format("tile.item_router.name"),
                cpm.getOperation().getSymbol(),
                I18n.format("guiText.label.playerSect." + cpm.getSection()));
        list.add(new TextComponentString(s));
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPlayerModule(router, stack);
    }

    @Override
    public Class<? extends GuiModule> getGuiClass() {
        return GuiModulePlayer.class;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public EnumActionResult onItemUse(ItemUseContext ctx) {
        if (ctx.getWorld().isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (ctx.getPlayer() != null && ctx.getPlayer().isSneaking()) {
            setOwner(ctx.getItem(), ctx.getPlayer());
            ctx.getPlayer().sendStatusMessage(new TextComponentTranslation("itemText.security.owner", ctx.getPlayer().getDisplayName()), false);
            return EnumActionResult.SUCCESS;
        } else {
            return super.onItemUse(ctx);
        }
    }

    @Override
    public Color getItemTint() {
        return new Color(255, 208, 144);
    }
}
