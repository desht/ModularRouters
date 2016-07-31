package me.desht.modularrouters.item.module;

import com.google.common.base.Joiner;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.logic.CompiledModuleSettings;
import me.desht.modularrouters.logic.execution.ModuleExecutor;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class AbstractModule extends ItemBase {
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

    public AbstractModule(String name) {
        super(name);
//        setMaxStackSize(1);
    }

    public abstract ModuleExecutor getExecutor();

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1; // return any value greater than zero
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        validateNBT(stack);
        if (!world.isRemote) {
            player.openGui(ModularRouters.instance, ModularRouters.GUI_MODULE, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
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

    protected static void validateNBT(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (!compound.hasKey("Flags")) {
            byte flags = 0x0;
            for (FilterSettings b : FilterSettings.values()) {
                if (b.getDefaultValue()) {
                    flags |= b.getMask();
                }
            }
            compound.setByte("Flags", flags);
            System.out.printf("default value: %02x\n", flags);
        }
    }

    public static boolean isBlacklist(ItemStack stack) {
        return checkFlag(stack, FilterSettings.BLACKLIST);
    }

    public static boolean ignoreMeta(ItemStack stack) {
        return checkFlag(stack, FilterSettings.IGNORE_META);
    }

    public static boolean ignoreNBT(ItemStack stack) {
        return checkFlag(stack, FilterSettings.IGNORE_NBT);
    }

    public static boolean ignoreOreDict(ItemStack stack) {
        return checkFlag(stack, FilterSettings.IGNORE_OREDICT);
    }

    public static boolean terminates(ItemStack stack) {
        return checkFlag(stack, FilterSettings.TERMINATE);
    }

    public static boolean checkFlag(ItemStack stack, FilterSettings flag) {
        validateNBT(stack);
        return (stack.getTagCompound().getByte("Flags") & flag.getMask()) != 0x0;
    }

    public static RelativeDirection getDirectionFromNBT(ItemStack stack) {
        validateNBT(stack);
        byte flags = stack.getTagCompound().getByte("Flags");
        return RelativeDirection.values()[(flags & 0x70) >> 4];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        if (GuiScreen.isShiftKeyDown()) {
            validateNBT(itemstack);
            NBTTagCompound compound = itemstack.getTagCompound();
            RelativeDirection dir = getDirectionFromNBT(itemstack);
            list.add(TextFormatting.YELLOW + I18n.format("guiText.label.direction") + ": " + TextFormatting.AQUA + I18n.format("guiText.label." + dir.name()));
            NBTTagList items = compound.getTagList("ModuleFilter", Constants.NBT.TAG_COMPOUND);
            list.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip.BLACKLIST." + (isBlacklist(itemstack) ? "2" : "1")) + ":");
            if (items.tagCount() > 0) {
                for (int i = 0; i < items.tagCount(); i++) {
                    ItemStack s = ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i));
                    list.add(" \u2022 " + TextFormatting.AQUA + s.getDisplayName());
                }
            } else {
                list.add("  " + TextFormatting.AQUA + TextFormatting.ITALIC + I18n.format("itemText.misc.noItems"));
            }
            list.add(TextFormatting.YELLOW + I18n.format("itemText.misc.flags") + ":");
            list.add("  " + Joiner.on(" | ").join(
                    compose("IGNORE_META", ignoreMeta(itemstack)),
                    compose("IGNORE_NBT", ignoreNBT(itemstack)),
                    compose("IGNORE_OREDICT", ignoreOreDict(itemstack)),
                    compose("TERMINATE", !terminates(itemstack))
            ));
        } else if (GuiScreen.isCtrlKeyDown()) {
            addUsageInformation(itemstack, player, list, par4);
        } else {
            list.add(I18n.format("itemText.misc.holdShift"));
        }
    }

    private String compose(String key, boolean flag) {
        String text = I18n.format("itemText.misc." + key);
        return TextFormatting.AQUA + (flag ? TextFormatting.STRIKETHROUGH.toString() : "") + text + TextFormatting.RESET;
    }

    protected void addUsageInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        MiscUtil.appendMultiline(list, "itemText.usage." + getUnlocalizedName(itemstack));
    }

    public Class<? extends CompiledModuleSettings> getCompiler() {
        return CompiledModuleSettings.class;
    }
}
