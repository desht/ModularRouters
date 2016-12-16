package me.desht.modularrouters.item.module;

import com.google.common.base.Joiner;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.SmartFilter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.util.InventoryUtils;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class ItemModule extends ItemBase {
    private static final String NBT_CONFIG_SLOT = "ConfigSlot";

    // add new types at the end!
    public enum ModuleType {
        BREAKER,
        DROPPER,
        PLACER,
        PULLER,
        SENDER1,
        SENDER2,
        SENDER3,
        SORTER,
        VACUUM,
        VOID,
        DETECTOR,
        MODSORTER,
        FLINGER,
        PLAYER,
        EXTRUDER,
        FLUID;

        public static ModuleType getType(ItemStack stack) {
            return stack.getItem() instanceof ItemModule ? values()[stack.getItemDamage()] : null;
        }
    }
    public static final int SUBTYPES = ModuleType.values().length;
    private static final Module[] modules = new Module[SUBTYPES];

    static {
        registerSubItem(ModuleType.BREAKER, new BreakerModule());
        registerSubItem(ModuleType.DROPPER, new DropperModule());
        registerSubItem(ModuleType.PLACER, new PlacerModule());
        registerSubItem(ModuleType.PULLER, new PullerModule());
        registerSubItem(ModuleType.SENDER1, new SenderModule1());
        registerSubItem(ModuleType.SENDER2, new SenderModule2());
        registerSubItem(ModuleType.SENDER3, new SenderModule3());
        registerSubItem(ModuleType.SORTER, new SorterModule());
        registerSubItem(ModuleType.VACUUM, new VacuumModule());
        registerSubItem(ModuleType.VOID, new VoidModule());
        registerSubItem(ModuleType.DETECTOR, new DetectorModule());
        registerSubItem(ModuleType.MODSORTER, new ModSorterModule());
        registerSubItem(ModuleType.FLINGER, new FlingerModule());
        registerSubItem(ModuleType.PLAYER, new PlayerModule());
        registerSubItem(ModuleType.EXTRUDER, new ExtruderModule());
        registerSubItem(ModuleType.FLUID, new FluidModule());
    }

    private static void registerSubItem(ModuleType type, Module handler) {
        modules[type.ordinal()] = handler;
    }

    public ItemModule() {
        super("module");
        setHasSubtypes(true);
        MinecraftForge.EVENT_BUS.register(ItemModule.class);
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide() == Side.CLIENT) {
            Module mod = getModule(event.getItemStack());
            if (!(mod instanceof TargetedModule)) {
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
    public void getSubItems(@Nonnull Item item, CreativeTabs tab, NonNullList<ItemStack> stacks) {
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
        return ItemModule.ModuleType.values()[meta].name().toLowerCase() + "_module";
    }

    public static Module getModule(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemModule)) {
            return null;
        }
        return stack.getItemDamage() < modules.length ? modules[stack.getItemDamage()] : null;
    }

    public static Module getModule(ModuleType type) {
        return modules[type.ordinal()];
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1; // return any value greater than zero
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ModuleHelper.validateNBT(stack);
        if (!world.isRemote) {
            int guiId = hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_MODULE_HELD_MAIN : ModularRouters.GUI_MODULE_HELD_OFF;
            player.openGui(ModularRouters.instance, guiId, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return getModule(stack).onEntitySwing(entityLiving, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
        ItemStack stack = player.getHeldItem(hand);
        return getModule(stack).onItemUse(stack, player, world, pos, hand, face, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, EntityPlayer player, List<String> list, boolean par4) {
        Module module = getModule(itemstack);
        if (module == null) {
            return;
        }
        module.addBasicInformation(itemstack, player, list, par4);

        if (GuiScreen.isShiftKeyDown()) {
            addSettingsInformation(itemstack, list, module);
            module.addExtraInformation(itemstack, player, list, par4);
        } else if (GuiScreen.isCtrlKeyDown()) {
            module.addUsageInformation(itemstack, player, list, par4);
        } else {
            list.add(I18n.format("itemText.misc.holdShiftCtrl"));
        }
    }

    private void addSettingsInformation(ItemStack itemstack, List<String> list, Module module) {
        if (module.isDirectional()) {
            Module.RelativeDirection dir = ModuleHelper.getDirectionFromNBT(itemstack);
            list.add(TextFormatting.YELLOW + I18n.format("guiText.label.direction") + ": " + TextFormatting.AQUA + I18n.format("guiText.tooltip." + dir.name()));
        }
        NBTTagList items = ModuleHelper.getFilterItems(itemstack);
        list.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip.BLACKLIST." + (ModuleHelper.isBlacklist(itemstack) ? "2" : "1")) + ":");
        if (items.tagCount() > 0) {
            for (int i = 0; i < items.tagCount(); i++) {
                ItemStack s = new ItemStack(items.getCompoundTagAt(i));
                SmartFilter f = ItemSmartFilter.getFilter(s);
                if (f == null) {
                    list.add(" \u2022 " + TextFormatting.AQUA + s.getDisplayName());
                } else {
                    int size = f.getSize(s);
                    String suffix = size > 0 ? " [" + f.getSize(s) + "]" : "";
                    list.add(" \u2022 " + TextFormatting.AQUA + TextFormatting.ITALIC + s.getDisplayName() + suffix);
                }
            }
        } else {
            list.add("  " + TextFormatting.AQUA + TextFormatting.ITALIC + I18n.format("itemText.misc.noItems"));
        }
        list.add(TextFormatting.YELLOW + I18n.format("itemText.misc.flags") + ": " +
                Joiner.on(" | ").join(
                        compose("IGNORE_META", ModuleHelper.ignoreMeta(itemstack)),
                        compose("IGNORE_NBT", ModuleHelper.ignoreNBT(itemstack)),
                        compose("IGNORE_OREDICT", ModuleHelper.ignoreOreDict(itemstack)),
                        compose("TERMINATE", !ModuleHelper.terminates(itemstack))
                ));
        if (ModuleHelper.isRedstoneBehaviourEnabled(itemstack)) {
            RouterRedstoneBehaviour rrb = ModuleHelper.getRedstoneBehaviour(itemstack);
            list.add(TextFormatting.GREEN + I18n.format("guiText.tooltip.redstone.label")
                    + ": " + TextFormatting.AQUA + I18n.format("guiText.tooltip.redstone." + rrb.toString()));
        }
        if (ModuleHelper.isRegulatorEnabled(itemstack)) {
            int amount = ModuleHelper.getRegulatorAmount(itemstack);
            list.add(TextFormatting.GREEN + I18n.format("guiText.tooltip.regulator.label", amount));
        }
        int pickupDelay = ModuleHelper.getPickupDelay(itemstack);
        if (pickupDelay > 0) {
            list.add(TextFormatting.GREEN + I18n.format("itemText.misc.pickupDelay", pickupDelay, pickupDelay / 20.0f));
        }
    }

    private String compose(String key, boolean flag) {
        String text = I18n.format("itemText.misc." + key);
        return (flag ? TextFormatting.DARK_GRAY + TextFormatting.STRIKETHROUGH.toString() : TextFormatting.AQUA) + text + TextFormatting.RESET;
    }

    public static ItemStack makeItemStack(ModuleType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(ModuleType type, int amount) {
        return new ItemStack(ModItems.module, amount, type.ordinal());
    }

    public static boolean isType(ItemStack stack, ModuleType type) {
        return stack != null && stack.getItem() instanceof ItemModule && stack.getItemDamage() == type.ordinal();
    }

    public static void setFilterConfigSlot(ItemStack stack, int slot) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
        }
        if (slot < 0) {
            compound.removeTag(NBT_CONFIG_SLOT);
        } else {
            compound.setInteger(NBT_CONFIG_SLOT, slot);
        }
        stack.setTagCompound(compound);
    }

    public static int getFilterConfigSlot(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_CONFIG_SLOT)) {
            return stack.getTagCompound().getInteger(NBT_CONFIG_SLOT);
        } else {
            return -1;
        }
    }
}
