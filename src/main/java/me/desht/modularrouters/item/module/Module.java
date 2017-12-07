package me.desht.modularrouters.item.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.container.slot.ValidatingSlot;
import me.desht.modularrouters.item.ItemSubTypes;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.augment.ItemAugment.AugmentCounter;
import me.desht.modularrouters.item.augment.ItemAugment.AugmentType;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class Module extends ItemSubTypes.SubItemHandler {
    public enum ModuleFlags {
        BLACKLIST(true, 0x1),
        IGNORE_META(false, 0x2),
        IGNORE_NBT(true, 0x4),
        IGNORE_OREDICT(true, 0x8),
        TERMINATE(false, 0x80);

        private final boolean defaultValue;

        private byte mask;

        ModuleFlags(boolean defaultValue, int mask) {
            this.defaultValue = defaultValue;
            this.mask = (byte) mask;
        }

        public boolean getDefaultValue() {
            return defaultValue;
        }
        public byte getMask() {
            return mask;
        }

    }

    // Direction relative to the facing of the router this module is installed in
    public enum RelativeDirection {
        NONE(0x00, null),
        DOWN(0x01, BlockItemRouter.OPEN_D),
        UP(0x02, BlockItemRouter.OPEN_U),
        LEFT(0x04, BlockItemRouter.OPEN_L),
        RIGHT(0x08, BlockItemRouter.OPEN_R),
        FRONT(0x10, BlockItemRouter.OPEN_F),
        BACK(0x20, BlockItemRouter.OPEN_B);
        private static RelativeDirection[] realSides = new RelativeDirection[] { FRONT, BACK, UP, DOWN, LEFT, RIGHT };

        private final int mask;
        private final PropertyBool property;
        RelativeDirection(int mask, PropertyBool property) {
            this.mask = mask;
            this.property = property;
        }

        public static RelativeDirection[] realSides() {
            return realSides;
        }

        public EnumFacing toEnumFacing(EnumFacing current) {
            switch (this) {
                case UP:
                    return EnumFacing.UP;
                case DOWN:
                    return EnumFacing.DOWN;
                case FRONT:
                    return current;
                case LEFT:
                    return current.rotateY();
                case BACK:
                    return current.getOpposite();
                case RIGHT:
                    return current.rotateYCCW();
                default:
                    return current;
            }
        }

        public int getMask() {
            return mask;
        }

        public PropertyBool getProperty() {
            return property;
        }
    }

    public abstract CompiledModule compile(TileEntityItemRouter router, ItemStack stack);

    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            Slot slot = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
            if (slot instanceof ValidatingSlot.Module) {
                list.add(MiscUtil.translate("itemText.misc.configureHint", String.valueOf(ConfigHandler.getConfigKey())));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        addSettingsInformation(itemstack, list);
        addAugmentInformation(itemstack, list);
    }

    private void addSettingsInformation(ItemStack itemstack, List<String> list) {
        if (isDirectional()) {
            Module.RelativeDirection dir = ModuleHelper.getDirectionFromNBT(itemstack);
            String dirStr = getDirectionString(dir);
            list.add(TextFormatting.YELLOW + I18n.format("guiText.label.direction") + ": " + TextFormatting.AQUA + dirStr);
        }
        addFilterInformation(itemstack, list);
        list.add(TextFormatting.YELLOW + I18n.format("itemText.misc.flags") + ": " +
                String.join(" / ",
                        formatFlag("IGNORE_META", ModuleHelper.ignoreMeta(itemstack)),
                        formatFlag("IGNORE_NBT", ModuleHelper.ignoreNBT(itemstack)),
                        formatFlag("IGNORE_OREDICT", ModuleHelper.ignoreOreDict(itemstack)),
                        formatFlag("TERMINATE", !ModuleHelper.terminates(itemstack))
                ));
        if (this instanceof IRangedModule) {
            IRangedModule rm = (IRangedModule) this;
            int curRange = rm.getCurrentRange(itemstack);
            String col = curRange > rm.getBaseRange() ?
                    TextFormatting.GREEN.toString() : curRange < rm.getBaseRange() ?
                    TextFormatting.RED.toString() : TextFormatting.AQUA.toString();
            list.add(TextFormatting.YELLOW + I18n.format("itemText.misc.rangeInfo",
                    col, rm.getCurrentRange(itemstack), rm.getBaseRange(), rm.getHardMaxRange()));
        }
    }

    public String getDirectionString(RelativeDirection dir) {
        return isOmniDirectional() && dir == RelativeDirection.NONE ?
                I18n.format("guiText.tooltip.allDirections") :
                I18n.format("guiText.tooltip." + dir.toString());
    }

    private String formatFlag(String key, boolean flag) {
        String text = I18n.format("itemText.misc." + key);
        return (flag ? TextFormatting.DARK_AQUA + TextFormatting.STRIKETHROUGH.toString() : TextFormatting.AQUA) + text + TextFormatting.RESET;
    }

    private void addFilterInformation(ItemStack itemstack, List<String> list) {
        NBTTagList filterItems = ModuleHelper.getFilterItems(itemstack);
        list.add(TextFormatting.YELLOW + I18n.format("itemText.misc." + (ModuleHelper.isBlacklist(itemstack) ? "blacklist" : "whitelist")) + ":");
        if (filterItems.tagCount() > 0) {
            for (int i = 0; i < filterItems.tagCount(); i++) {
                ItemStack s = new ItemStack(filterItems.getCompoundTagAt(i));
                SmartFilter f = ItemSmartFilter.getFilter(s);
                if (f == null) {
                    list.add(" \u2022 " + TextFormatting.AQUA + s.getDisplayName());
                } else {
                    int size = f.getSize(s);
                    String suffix = size > 0 ? " [" + size + "]" : "";
                    list.add(" \u2022 " + TextFormatting.AQUA + TextFormatting.ITALIC + s.getDisplayName() + suffix);
                }
            }
        } else {
            String s = list.get(list.size() - 1);
            list.set(list.size() - 1, s + " " + TextFormatting.AQUA + TextFormatting.ITALIC + I18n.format("itemText.misc.noItems"));
        }
    }

    private void addAugmentInformation(ItemStack itemstack, List<String> list) {
        AugmentCounter c = new AugmentCounter(itemstack);

        List<String> toAdd = Lists.newArrayList();
        for (AugmentType type: AugmentType.values()) {
            int n = c.getAugmentCount(type);
            if (n > 0) {
                String s = I18n.format("item." + type.toString().toLowerCase() + "_augment.name");
                if (n > 1) s = n + " x " + s;
                s += TextFormatting.AQUA + ItemAugment.getAugment(type).getExtraInfo(n, itemstack);
                toAdd.add(" \u2022 " + TextFormatting.GREEN + s);
            }
        }
        if (!toAdd.isEmpty()) {
            list.add(TextFormatting.YELLOW + I18n.format("itemText.augments"));
            list.addAll(toAdd);
        }
    }

    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(world, pos);
        if (router != null) {
            if (!player.isSneaking()) {
                player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, world, pos.getX(), pos.getY(), pos.getZ());
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    public ActionResult<ItemStack> onSneakRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return false;
    }

    public ContainerModule createGuiContainer(EntityPlayer player, EnumHand hand, ItemStack moduleStack, TileEntityItemRouter router) {
        return new ContainerModule(player, hand, moduleStack, router);
    }

    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModule.class;
    }

    public boolean isDirectional() {
        return true;
    }

    public boolean isOmniDirectional() { return false; }

    public boolean isFluidModule() {
        return false;
    }

    /**
     * Check if the given item is OK for this module's filter.
     *
     * @param stack the item to check
     * @return true if the item may be inserted in the module's filter, false otherwise
     */
    public boolean isItemValidForFilter(ItemStack stack) {
        return true;
    }

    /**
     * Get the item matcher to be used for simple items, i.e. not smart filters.
     *
     * @param stack the item to be matched
     * @return an item matcher object
     */
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new SimpleItemMatcher(stack);
    }
}
