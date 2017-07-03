package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.core.RegistrarMR;
import me.desht.modularrouters.item.ItemSubTypes;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
public class ItemSmartFilter extends ItemSubTypes<ItemSmartFilter.FilterType> {
    public enum FilterType {
        BULKITEM,
        MOD,
        REGEX,
        INSPECTION;

        public static FilterType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemSmartFilter ? values()[stack.getItemDamage()] : null;
        }
    }

    public ItemSmartFilter() {
        super("filter", FilterType.class);

        register(FilterType.BULKITEM, new BulkItemFilter());
        register(FilterType.MOD, new ModFilter());
        register(FilterType.REGEX, new RegexFilter());
        register(FilterType.INSPECTION, new InspectionFilter());
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == Side.CLIENT) {
            SubItemHandler filter = ItemSmartFilter.getFilter(event.getItemStack());
            if (filter == null) {
                return;
            }
            if (InventoryUtils.getInventory(event.getWorld(), event.getPos(), event.getFace()) != null) {
                return;
            }
            // We're right-clicking an ordinary block; canceling this prevents the onArmSwing() method
            // being called, and allows the GUI to be opened normally.
            event.setCanceled(true);
        }
    }

    public static SmartFilter getFilter(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemSmartFilter)) {
            return null;
        }
        if (stack.getMetadata() >= FilterType.values().length) {
            return null;
        }
        return getFilter(FilterType.values()[stack.getMetadata()]);
    }

    public static SmartFilter getFilter(FilterType type) {
        return (SmartFilter) RegistrarMR.FILTER.getHandler(type);
    }

    public static boolean isType(ItemStack stack, FilterType type) {
        return stack.getItem() instanceof ItemSmartFilter && stack.getItemDamage() == type.ordinal();
    }

    public static ItemStack makeItemStack(FilterType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(FilterType type, int amount) {
        return new ItemStack(RegistrarMR.FILTER, amount, type.ordinal());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        SmartFilter filter = getFilter(stack);
        int guiId = hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_FILTER_HELD_MAIN : ModularRouters.GUI_FILTER_HELD_OFF;
        if (!world.isRemote && filter.hasGuiContainer() || world.isRemote && !filter.hasGuiContainer()) {
            player.openGui(ModularRouters.instance, guiId, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        ItemStack stack = player.getHeldItem(hand);
        return getHandler(stack).onItemUse(stack, player, world, pos, hand, face, x, y, z);
    }
}
