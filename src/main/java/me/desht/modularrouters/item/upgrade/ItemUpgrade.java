package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.ItemSubTypes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemUpgrade extends ItemSubTypes<ItemUpgrade.UpgradeType> {

    public enum UpgradeType {
        STACK,
        SPEED,
        SECURITY,
        CAMOUFLAGE,
        SYNC,
        FLUID,
        MUFFLER,
        BLAST;

        public static UpgradeType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemUpgrade ? values()[stack.getItemDamage()] : null;
        }
    }

    public ItemUpgrade() {
        super("upgrade", UpgradeType.class);

        register(UpgradeType.STACK, new StackUpgrade());
        register(UpgradeType.SPEED, new SpeedUpgrade());
        register(UpgradeType.SECURITY, new SecurityUpgrade());
        register(UpgradeType.CAMOUFLAGE, new CamouflageUpgrade());
        register(UpgradeType.SYNC, new SyncUpgrade());
        register(UpgradeType.FLUID, new FluidUpgrade());
        register(UpgradeType.MUFFLER, new MufflerUpgrade());
        register(UpgradeType.BLAST, new BlastUpgrade());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        return getUpgrade(stack).onItemRightClick(stack, worldIn, playerIn, handIn);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        return getUpgrade(stack).onItemUse(stack, player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    public static ItemStack makeItemStack(UpgradeType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(UpgradeType type, int amount) {
        return new ItemStack(RegistrarMR.UPGRADE, amount, type.ordinal());
    }

    public static Upgrade getUpgrade(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemUpgrade)) {
            return null;
        }
        if (stack.getMetadata() >= UpgradeType.values().length) {
            return null;
        }
        return getUpgrade(UpgradeType.values()[stack.getMetadata()]);
    }

    public static Upgrade getUpgrade(UpgradeType type) {
        return (Upgrade) RegistrarMR.UPGRADE.getHandler(type);
    }

    public static boolean isType(ItemStack stack, UpgradeType type) {
        return stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() == type.ordinal();
    }
}
