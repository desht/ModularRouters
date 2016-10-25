package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ValidatingSlot;
import me.desht.modularrouters.gui.GuiItemRouter;
import me.desht.modularrouters.gui.module.GuiModule;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class Module {

    public static final String NBT_FLAGS = "Flags";
    public static final String NBT_REDSTONE_ENABLED = "RedstoneEnabled";
    public static final String NBT_REDSTONE_MODE = "RedstoneMode";

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
        return (compound.getByte(NBT_FLAGS) & flag.getMask()) != 0x0;
    }

    public RelativeDirection getDirectionFromNBT(ItemStack stack) {
        if (!isDirectional()) {
            return RelativeDirection.NONE;
        }
        NBTTagCompound compound = validateNBT(stack);
        return RelativeDirection.values()[(compound.getByte(NBT_FLAGS) & 0x70) >> 4];
    }

    public boolean isRedstoneBehaviourEnabled(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        return compound.getBoolean(NBT_REDSTONE_ENABLED);
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour(ItemStack stack) {
        NBTTagCompound compound = validateNBT(stack);
        if (compound.getBoolean(NBT_REDSTONE_ENABLED) && compound.hasKey(NBT_REDSTONE_MODE)) {
            try {
                return RouterRedstoneBehaviour.valueOf(compound.getString(NBT_REDSTONE_MODE));
            } catch (IllegalArgumentException e) {
                return RouterRedstoneBehaviour.ALWAYS;
            }
        } else {
            return RouterRedstoneBehaviour.ALWAYS;
        }
    }

    /**
     * Basic information for the module, which is always shown.
     */
    @SideOnly(Side.CLIENT)
    public void addBasicInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            Slot slot = ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
            if (slot instanceof ValidatingSlot.Module) {
                list.add(MiscUtil.translate("itemText.misc.configureHint", String.valueOf(Config.configKey)));
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

    public Object[] getExtraUsageParams() {
        return new Object[0];
    }

    @Nonnull
    public static NBTTagCompound validateNBT(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (!compound.hasKey(NBT_FLAGS)) {
            byte flags = 0x0;
            for (ModuleFlags b : ModuleFlags.values()) {
                if (b.getDefaultValue()) {
                    flags |= b.getMask();
                }
            }
            compound.setByte(NBT_FLAGS, flags);
        }
        return compound;
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

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return false;
    }

    public Class<? extends GuiModule> getGuiHandler() {
        return GuiModule.class;
    }

    public boolean isDirectional() {
        return true;
    }

    public abstract IRecipe getRecipe();
}
