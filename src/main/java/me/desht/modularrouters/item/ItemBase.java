package me.desht.modularrouters.item;

import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemBase extends Item {

    public ItemBase(Properties props) {
        super(props);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        if (world == null) return;

        if (GuiScreen.isCtrlKeyDown()) {
            addUsageInformation(stack, list);
        } else if (ConfigHandler.CLIENT_MISC.alwaysShowModuleSettings.get() || GuiScreen.isShiftKeyDown()) {
            addExtraInformation(stack, list);
            list.add(new TextComponentTranslation("itemText.misc.holdCtrl").applyTextStyles(TextFormatting.GRAY));
        } else if (!ConfigHandler.CLIENT_MISC.alwaysShowModuleSettings.get()) {
            list.add(new TextComponentTranslation("itemText.misc.holdShiftCtrl").applyTextStyles(TextFormatting.GRAY));
        }
    }

    protected void addUsageInformation(ItemStack itemstack, List<ITextComponent> list) {
        MiscUtil.appendMultilineText(list, TextFormatting.GRAY,
                "itemText.usage.item." + itemstack.getItem().getRegistryName().getPath(), getExtraUsageParams());
    }

    protected abstract void addExtraInformation(ItemStack stack, List<ITextComponent> list);

    protected Object[] getExtraUsageParams() {
        return new Object[0];
    }

}
