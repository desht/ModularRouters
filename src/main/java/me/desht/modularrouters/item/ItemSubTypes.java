package me.desht.modularrouters.item;

import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

public abstract class ItemSubTypes<T extends Enum<T>> extends ItemBase {
    private final SubItemHandler[] handlers;
    private final Class<T> value;

    public ItemSubTypes(String name, Class<T> value) {
        super(name);

        this.value = value;
        this.handlers = new SubItemHandler[getSubTypes()];

        setHasSubtypes(true);
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack stack) {
        return "item." + getSubTypeName(stack.getMetadata());
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (int i = 0; i < getSubTypes(); i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    public int getSubTypes() {
        T[] vals = value.getEnumConstants();
        return vals.length;
    }

    public String getSubTypeName(int meta) {
        return value.getEnumConstants()[meta].name().toLowerCase() + "_" + name;
    }

    protected void register(T val, SubItemHandler handler) {
        handlers[val.ordinal()] = handler;
    }

    public SubItemHandler getHandler(ItemStack stack) {
        return handlers[stack.getMetadata()];
    }

    public SubItemHandler getHandler(T what) {
        return handlers[what.ordinal()];
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1; // return any value greater than zero
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        SubItemHandler handler = getHandler(itemstack);
        if (handler != null) {
            handler.addBasicInformation(itemstack, player, list, advanced);
            if (GuiScreen.isCtrlKeyDown()) {
                handler.addUsageInformation(itemstack, player, list, advanced);
            } else if (ConfigHandler.misc.alwaysShowSettings || GuiScreen.isShiftKeyDown()) {
                handler.addExtraInformation(itemstack, player, list, advanced);
                list.add(I18n.format("itemText.misc.holdCtrl"));
            } else if (!ConfigHandler.misc.alwaysShowSettings) {
                list.add(I18n.format("itemText.misc.holdShiftCtrl"));
            }
        }
    }

    public static class SubItemHandler {
        /**
         * Basic tooltip information, always shown.
         *
         * @param itemstack the item
         * @param player the player
         * @param list tooltip text
         * @param advanced advanced flags
         */
        @SideOnly(Side.CLIENT)
        public void addBasicInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        }

        /**
         * Usage information, shown when Ctrl is held.
         *
         * @param itemstack the item
         * @param player the player
         * @param list tooltip text
         * @param advanced advanced flags
         */
        @SideOnly(Side.CLIENT)
        public void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
            String s = I18n.format("itemText.usage." + itemstack.getTranslationKey(), getExtraUsageParams());
            for (String s1 : s.split("\\\\n")) {
                list.addAll(MiscUtil.wrapString(s1));
            }
        }

        /**
         * Extra information, show with Shift is held OR "alwaysShowSettings" is true in config.
         *
         * @param itemstack the item
         * @param player the player
         * @param list tooltip text
         * @param advanced advanced flags
         */
        @SideOnly(Side.CLIENT)
        public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        }

        public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float x, float y, float z) {
            return EnumActionResult.PASS;
        }

        /**
         * If an item has extra data to pass to the translation string for its usage tooltip, that can be returned here.
         *
         * @return an array of extra usage information
         */
        public Object[] getExtraUsageParams() {
            return new Object[0];
        }

        @SideOnly(Side.CLIENT)
        public Color getItemTint() {
            return Color.WHITE;
        }
    }
}
