package me.desht.modularrouters.item.module;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.gui.module.GuiModulePlayer;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class PlayerModule extends Module {
    @Override
    protected void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        super.addExtraInformation(itemstack, player, list, advanced);
        CompiledPlayerModule cpm = new CompiledPlayerModule(null, itemstack);
        list.add(TextFormatting.YELLOW + I18n.format("itemText.security.owner", cpm.getPlayerName()));
        list.add(TextFormatting.YELLOW + String.format(TextFormatting.YELLOW + "%s: " + TextFormatting.AQUA + "%s %s %s",
                I18n.format("itemText.misc.operation"),
                I18n.format("tile.item_router.name"),
                cpm.getOperation().getSymbol(),
                I18n.format("guiText.label.playerSect." + cpm.getSection())));
    }

    @Override
    public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledPlayerModule(router, stack);
    }

    @Override
    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModulePlayer.class;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float x, float y, float z) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (player.isSneaking()) {
            ModuleHelper.setOwner(stack, player);
            player.sendStatusMessage(new TextComponentTranslation("itemText.security.owner", player.getDisplayNameString()), false);
            return EnumActionResult.SUCCESS;
        } else {
            return super.onItemUse(stack, player, world, pos, hand, face, x, y, z);
        }
    }
}
