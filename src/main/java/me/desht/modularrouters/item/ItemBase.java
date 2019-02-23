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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemBase extends Item {

    public ItemBase(Properties props) {
        super(props);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        if (GuiScreen.isCtrlKeyDown()) {
            addUsageInformation(stack, list);
        } else if (ConfigHandler.MISC.alwaysShowSettings.get() || GuiScreen.isShiftKeyDown()) {
            addExtraInformation(stack, list);
            list.add(new TextComponentTranslation("itemText.misc.holdCtrl"));
        } else if (!ConfigHandler.MISC.alwaysShowSettings.get()) {
            list.add(new TextComponentTranslation("itemText.misc.holdShiftCtrl"));
        }
    }

    protected void addUsageInformation(ItemStack itemstack, List<ITextComponent> list) {
        String s = I18n.format("itemText.usage." + itemstack.getTranslationKey(), getExtraUsageParams());
        for (String s1 : s.split("\\\\n")) {
            for (String s2 : MiscUtil.wrapString(s1)) {
                list.add(new TextComponentString(s2));
            }
        }
    }

    protected abstract void addExtraInformation(ItemStack stack, List<ITextComponent> list);

    protected Object[] getExtraUsageParams() {
        return new Object[0];
    }

}
