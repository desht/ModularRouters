package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ValidatingSlot;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.util.MiscUtil;
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

import java.util.List;

public abstract class Module {

    public enum FilterSettings {
        BLACKLIST(true, 0x1),
        IGNORE_META(false, 0x2),
        IGNORE_NBT(true, 0x4),
        IGNORE_OREDICT(true, 0x8),
        TERMINATE(false, 0x80);

        private final boolean defaultValue;

        private byte mask;
        FilterSettings(boolean defaultValue, int mask) {
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
        NONE,
        DOWN,
        UP,
        LEFT,
        RIGHT,
        FRONT,
        BACK;

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

    }
    public CompiledModuleSettings compile(ItemStack stack) {
        return new CompiledModuleSettings(stack);
    }

    public abstract boolean execute(TileEntityItemRouter router, CompiledModuleSettings settings);

    public static boolean isBlacklist(ItemStack stack) {
        return checkFlag(stack, Module.FilterSettings.BLACKLIST);
    }

    public static boolean ignoreMeta(ItemStack stack) {
        return checkFlag(stack, Module.FilterSettings.IGNORE_META);
    }

    public static boolean ignoreNBT(ItemStack stack) {
        return checkFlag(stack, Module.FilterSettings.IGNORE_NBT);
    }

    public static boolean ignoreOreDict(ItemStack stack) {
        return checkFlag(stack, Module.FilterSettings.IGNORE_OREDICT);
    }

    public static boolean terminates(ItemStack stack) {
        return checkFlag(stack, Module.FilterSettings.TERMINATE);
    }

    public static boolean checkFlag(ItemStack stack, Module.FilterSettings flag) {
        validateNBT(stack);
        return (stack.getTagCompound().getByte("Flags") & flag.getMask()) != 0x0;
    }

    public static RelativeDirection getDirectionFromNBT(ItemStack stack) {
        validateNBT(stack);
        byte flags = stack.getTagCompound().getByte("Flags");
        return RelativeDirection.values()[(flags & 0x70) >> 4];
    }

    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            Slot slot = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
            if (slot instanceof ValidatingSlot.Module) {
                list.add(I18n.translateToLocalFormatted("itemText.misc.configureHint", String.valueOf(Config.configKey)));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + itemstack.getItem().getUnlocalizedName(itemstack), getExtraUsageParams());
    }

    protected Object[] getExtraUsageParams() {
        return new Object[0];
    }

    protected static void validateNBT(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (!compound.hasKey("Flags")) {
            byte flags = 0x0;
            for (Module.FilterSettings b : Module.FilterSettings.values()) {
                if (b.getDefaultValue()) {
                    flags |= b.getMask();
                }
            }
            compound.setByte("Flags", flags);
        }
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

}
