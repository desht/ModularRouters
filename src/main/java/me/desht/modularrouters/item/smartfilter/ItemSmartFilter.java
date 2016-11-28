package me.desht.modularrouters.item.smartfilter;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.util.InventoryUtils;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemSmartFilter extends ItemBase {
    public enum FilterType {
        BULKITEM,
        MOD,
        REGEX,
        INSPECTION;

        public static FilterType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemSmartFilter ? values()[stack.getItemDamage()] : null;
        }
    }

    public static final int SUBTYPES = FilterType.values().length;
    private static final SmartFilter[] filters = new SmartFilter[SUBTYPES];

    static {
        registerSubItem(FilterType.BULKITEM, new BulkItemFilter());
        registerSubItem(FilterType.MOD, new ModFilter());
        registerSubItem(FilterType.REGEX, new RegexFilter());
        registerSubItem(FilterType.INSPECTION, new InspectionFilter());
    }

    private static void registerSubItem(FilterType type, SmartFilter handler) {
        filters[type.ordinal()] = handler;
    }

    public ItemSmartFilter() {
        super("filter");
        setHasSubtypes(true);
        MinecraftForge.EVENT_BUS.register(ItemSmartFilter.class);
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == Side.CLIENT) {
            SmartFilter filter = getFilter(event.getItemStack());
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

    @Override
    public void getSubItems(@Nonnull Item item, CreativeTabs tab, List<ItemStack> stacks) {
        for (int i = 0; i < SUBTYPES; i++) {
            stacks.add(new ItemStack(item, 1, i));
        }
    }

    @Nonnull
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item." + getSubTypeName(stack.getItemDamage());
    }

    @Override
    public String getSubTypeName(int meta) {
        return ItemSmartFilter.FilterType.values()[meta].name().toLowerCase() + "Filter";
    }

    public static SmartFilter getFilter(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemSmartFilter)) {
            return null;
        }
        return stack.getItemDamage() < filters.length ? filters[stack.getItemDamage()] : null;
    }

    public static SmartFilter getFilter(FilterType type) {
        return filters[type.ordinal()];
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1; // return any value greater than zero
    }

    public static ItemStack makeItemStack(FilterType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(FilterType type, int amount) {
        return new ItemStack(ModItems.smartFilter, amount, type.ordinal());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        SmartFilter filter = getFilter(stack);
        int guiId = hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_FILTER_HELD_MAIN : ModularRouters.GUI_FILTER_HELD_OFF;
        if (!world.isRemote && filter.hasGuiContainer() || world.isRemote && !filter.hasGuiContainer()) {
            player.openGui(ModularRouters.instance, guiId, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        return getFilter(stack).onItemUse(stack, player, world, pos, hand, face, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        SmartFilter filter = getFilter(itemstack);
        if (filter == null) {
            return;
        }

        filter.addBasicInformation(itemstack, player, list, par4);
        if (GuiScreen.isShiftKeyDown()) {
            filter.addExtraInformation(itemstack, player, list, par4);
        } else if (GuiScreen.isCtrlKeyDown()) {
            filter.addUsageInformation(itemstack, player, list, par4);
        } else {
            list.add(I18n.format("itemText.misc.holdShiftCtrl"));
        }
    }

}
