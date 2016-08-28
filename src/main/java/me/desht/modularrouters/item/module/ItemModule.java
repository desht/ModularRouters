package me.desht.modularrouters.item.module;

import com.google.common.base.Joiner;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.util.InventoryUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemModule extends ItemBase {
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
        FLINGER
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
            if (!(mod instanceof TargetedSender)) {
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

//    @SubscribeEvent
//    public static void onPlayerInteract(PlayerInteractEvent event) {
//        if (event.getSide() == Side.SERVER
//                && (event instanceof PlayerInteractEvent.LeftClickBlock || event instanceof PlayerInteractEvent.LeftClickEmpty)) {
//            Module mod = ItemModule.getModule(event.getItemStack());
//            if (mod instanceof TargetedSender) {
//                ((TargetedSender) mod).showTargetInfo(event.getEntityPlayer(), event.getItemStack());
//                event.setCanceled(true);
//            }
//        }
//    }

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
        return ItemModule.ModuleType.values()[meta].name().toLowerCase() + "Module";
    }

    public static Module getModule(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemModule)) {
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
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        Module.validateNBT(stack);
        if (!world.isRemote) {
            int guiId = hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_MODULE_HELD_MAIN : ModularRouters.GUI_MODULE_HELD_OFF;
            player.openGui(ModularRouters.instance, guiId, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return getModule(stack).onEntitySwing(entityLiving, stack);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing face, float x, float y, float z) {
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
            NBTTagCompound compound =  Module.validateNBT(itemstack);
            Module.RelativeDirection dir = module.getDirectionFromNBT(itemstack);
            list.add(TextFormatting.YELLOW + I18n.format("guiText.label.direction") + ": " + TextFormatting.AQUA + I18n.format("guiText.tooltip." + dir.name()));
            NBTTagList items = compound.getTagList("ModuleFilter", Constants.NBT.TAG_COMPOUND);
            list.add(TextFormatting.YELLOW + I18n.format("guiText.tooltip.BLACKLIST." + (module.isBlacklist(itemstack) ? "2" : "1")) + ":");
            if (items.tagCount() > 0) {
                for (int i = 0; i < items.tagCount(); i++) {
                    ItemStack s = ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(i));
                    list.add(" \u2022 " + TextFormatting.AQUA + s.getDisplayName());
                }
            } else {
                list.add("  " + TextFormatting.AQUA + TextFormatting.ITALIC + I18n.format("itemText.misc.noItems"));
            }
            list.add(TextFormatting.YELLOW + I18n.format("itemText.misc.flags") + ": " +
                    Joiner.on(" | ").join(
                            compose("IGNORE_META", module.ignoreMeta(itemstack)),
                            compose("IGNORE_NBT", module.ignoreNBT(itemstack)),
                            compose("IGNORE_OREDICT", module.ignoreOreDict(itemstack)),
                            compose("TERMINATE", !module.terminates(itemstack))
                    ));
            module.addExtraInformation(itemstack, player, list, par4);
        } else if (GuiScreen.isCtrlKeyDown()) {
            module.addUsageInformation(itemstack, player, list, par4);
        } else {
            list.add(I18n.format("itemText.misc.holdShift"));
        }
    }

    private String compose(String key, boolean flag) {
        String text = I18n.format("itemText.misc." + key);
        return (flag ? TextFormatting.DARK_AQUA + TextFormatting.STRIKETHROUGH.toString() : TextFormatting.AQUA) + text + TextFormatting.RESET;
    }

    public static ItemStack makeItemStack(ModuleType type) {
        return makeItemStack(type, 1);
    }

    public static ItemStack makeItemStack(ModuleType type, int amount) {
        return new ItemStack(ModItems.module, amount, type.ordinal());
    }
}
