package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ValidatingSlot;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.gui.GuiModule;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.logic.RouterTarget;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
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

    public CompiledModuleSettings compile(TileEntityItemRouter router, ItemStack stack) {
        return new CompiledModuleSettings(router, stack);
    }

    public abstract boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings);

    public boolean isBlacklist(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.BLACKLIST);
    }

    public boolean ignoreMeta(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.IGNORE_META);
    }

    public boolean ignoreNBT(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.IGNORE_NBT);
    }

    public boolean ignoreOreDict(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.IGNORE_OREDICT);
    }

    public boolean terminates(ItemStack stack) {
        return checkFlag(stack, ModuleFlags.TERMINATE);
    }

    public boolean checkFlag(ItemStack stack, ModuleFlags flag) {
        NBTTagCompound compound = validateNBT(stack);
        return (compound.getByte("Flags") & flag.getMask()) != 0x0;
    }

    public RelativeDirection getDirectionFromNBT(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return RelativeDirection.values()[(compound.getByte("Flags") & 0x70) >> 4];
    }

    public RouterTarget getTarget(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) {
            return null;
        }
        EnumFacing facing = router.getAbsoluteFacing(getDirectionFromNBT(stack));
        return new RouterTarget(router.getWorld().provider.getDimension(), router.getPos().offset(facing), facing.getOpposite());
    }

    /**
     * Basic information for the module, which is always shown.
     */
    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            Slot slot = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
            if (slot instanceof ValidatingSlot.Module) {
                list.add(I18n.translateToLocalFormatted("itemText.misc.configureHint", String.valueOf(Config.configKey)));
            }
        }
    }

    /**
     * Usage information for the module, shown when Ctrl is held.
     */
    @SideOnly(Side.CLIENT)
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + itemstack.getItem().getUnlocalizedName(itemstack), getExtraUsageParams());
    }

    /**
     * Extra information for the module, shown when Shift is held.
     */
    @SideOnly(Side.CLIENT)
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean par4) {
        // nothing by default
    }

    protected Object[] getExtraUsageParams() {
        return new Object[0];
    }

    @Nonnull
    public static NBTTagCompound validateNBT(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (!compound.hasKey("Flags")) {
            byte flags = 0x0;
            for (ModuleFlags b : ModuleFlags.values()) {
                if (b.getDefaultValue()) {
                    flags |= b.getMask();
                }
            }
            compound.setByte("Flags", flags);
        }
        return compound;
    }

    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityItemRouter) {
            if (!player.isSneaking()) {
                player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER, world, pos.getX(), pos.getY(), pos.getZ());
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return false;
    }

    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModule.class;
    }

    public boolean isDirectional() {
        return true;
    }
}
