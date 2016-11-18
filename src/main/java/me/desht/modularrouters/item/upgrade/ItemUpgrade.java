package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ModItems;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemUpgrade extends ItemBase {

    public enum UpgradeType {
        STACK,
        SPEED,
        RANGE,
        SECURITY,
        CAMOUFLAGE,
        SYNC,
        FLUID;

        public static UpgradeType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemUpgrade ? values()[stack.getItemDamage()] : null;
        }
    }

    public static final int SUBTYPES = UpgradeType.values().length;
    private static final Upgrade[] upgrades = new Upgrade[SUBTYPES];

    static {
        registerUpgrade(UpgradeType.STACK, new StackUpgrade());
        registerUpgrade(UpgradeType.SPEED, new SpeedUpgrade());
        registerUpgrade(UpgradeType.RANGE, new RangeUpgrade());
        registerUpgrade(UpgradeType.SECURITY, new SecurityUpgrade());
        registerUpgrade(UpgradeType.CAMOUFLAGE, new CamouflageUpgrade());
        registerUpgrade(UpgradeType.SYNC, new SyncUpgrade());
        registerUpgrade(UpgradeType.FLUID, new FluidUpgrade());
    }

    public ItemUpgrade() {
        super("upgrade");
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(@Nonnull Item item, CreativeTabs tab, List<ItemStack> stacks) {
        for (int i = 0; i < SUBTYPES; i++) {
            stacks.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        Upgrade upgrade = getUpgrade(itemstack);
        if (upgrade != null) {
            upgrade.addBasicInformation(itemstack, player, list, par4);
            if (GuiScreen.isShiftKeyDown()) {
                upgrade.addExtraInformation(itemstack, player, list, par4);
            } else if (GuiScreen.isCtrlKeyDown()) {
                upgrade.addUsageInformation(itemstack, player, list, par4);
            } else {
                list.add(I18n.format(upgrade.hasExtraInformation() ? "itemText.misc.holdShiftCtrl" : "itemText.misc.holdCtrl"));
            }
        }
    }

    @Nonnull
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + getSubTypeName(stack.getItemDamage());
    }

    @Override
    public String getSubTypeName(int meta) {
        return UpgradeType.values()[meta].name().toLowerCase() + "Upgrade";
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        return getUpgrade(itemStackIn).onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return getUpgrade(stack).onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    private static void registerUpgrade(UpgradeType type, Upgrade handler) {
        upgrades[type.ordinal()] = handler;
    }

    public static ItemStack makeItemStack(UpgradeType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(UpgradeType type, int amount) {
        return new ItemStack(ModItems.upgrade, amount, type.ordinal());
    }

    public static Upgrade getUpgrade(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() < upgrades.length ? upgrades[stack.getItemDamage()] : null;
    }

    public static Upgrade getUpgrade(UpgradeType type) {
        return upgrades[type.ordinal()];
    }

    public static boolean isType(ItemStack stack, UpgradeType type) {
        return stack != null && stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() == type.ordinal();
    }
}
