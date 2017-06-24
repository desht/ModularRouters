package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.ConfigHandler;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.container.slot.ValidatingSlot;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.List;

public abstract class Module {
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

    /**
     * Basic information for the module, which is always shown.
     */
    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            Slot slot = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
            if (slot instanceof ValidatingSlot.Module) {
                list.add(MiscUtil.translate("itemText.misc.configureHint", String.valueOf(ConfigHandler.getConfigKey())));
            }
        }
    }

    /**
     * Usage information for the module, shown when Ctrl is held.
     */
    @SideOnly(Side.CLIENT)
    protected void addUsageInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        MiscUtil.appendMultiline(list, "itemText.usage." + itemstack.getItem().getUnlocalizedName(itemstack));
    }

    /**
     * Extra information for the module, shown when Shift is held.
     */
    @SideOnly(Side.CLIENT)
    protected void addExtraInformation(ItemStack itemstack, World player, List<String> list, ITooltipFlag advanced) {
        // nothing by default
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

    public boolean isFluidModule() {
        return false;
    }

    public boolean canBeRegulated() {
        return true;
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

    ShapelessOreRecipe makeShapelessOreRecipe(ItemStack result, Object... recipe) {
        return new ShapelessOreRecipe(new ResourceLocation(ModularRouters.MODID, "module_recipe"), result, recipe);
    }

    ShapedOreRecipe makeShapedOreRecipe(ItemStack result, Object... recipe) {
        return new ShapedOreRecipe(new ResourceLocation(ModularRouters.MODID, "module_recipe"), result, recipe);
    }
}
