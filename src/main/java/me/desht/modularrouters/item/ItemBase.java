package me.desht.modularrouters.item;

import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.config.MRConfig;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

        ITextComponent text = ClientSetup.keybindModuleInfo.func_238171_j_();

        if (ClientSetup.keybindModuleInfo.isKeyDown()) {
            addUsageInformation(stack, list);
        } else if (MRConfig.Client.Misc.alwaysShowModuleSettings || Screen.hasShiftDown()) {
            addExtraInformation(stack, list);
            if (ClientUtil.thisScreenPassesEvents()) {
                // TODO 1.16 func_240699_a_() = applyTextStyle()
                list.add(new TranslationTextComponent("itemText.misc.holdKey", text.getString()).func_240699_a_(TextFormatting.GRAY));
            }
        } else if (!MRConfig.Client.Misc.alwaysShowModuleSettings) {
            if (ClientUtil.thisScreenPassesEvents()) {
                list.add(new TranslationTextComponent("itemText.misc.holdShiftKey", text.getString()).func_240699_a_(TextFormatting.GRAY));
            }
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
