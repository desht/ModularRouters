package me.desht.modularrouters.item.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.BlockItemRouter;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.Keybindings;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.gui.module.GuiModule;
import me.desht.modularrouters.container.BaseContainerProvider;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.container.slot.ValidatingSlot;
import me.desht.modularrouters.core.ITintable;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.augment.ItemAugment.AugmentCounter;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import me.desht.modularrouters.util.SlotTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

@Mod.EventBusSubscriber
public abstract class ItemModule extends ItemBase implements ITintable {
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
        private final BooleanProperty property;
        RelativeDirection(int mask, BooleanProperty property) {
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

        public BooleanProperty getProperty() {
            return property;
        }
    }

    public ItemModule(Properties props) {
        super(props);
    }

    public abstract CompiledModule compile(TileEntityItemRouter router, ItemStack stack);

    public Class<? extends GuiModule> getGuiClass() {
        return GuiModule.class;
    }

    public abstract Color getItemTint();

    public boolean isDirectional() {
        return true;
    }

    public boolean isOmniDirectional() { return false; }

    public boolean isFluidModule() {
        return false;
    }

    public ContainerModule createContainer(EntityPlayer player, EnumHand hand, ItemStack stack, TileEntityItemRouter router) {
        return new ContainerModule(player.inventory, hand, stack, router);
    }

    /**
     * Check if the given item is OK for this module's filter.
     *
     * @param stack the item to check
     * @return true if the item may be inserted in the module's filter, false otherwise
     */
    public boolean isItemValidForFilter(ItemStack stack) {
        return true;
    }

    /**
     * Get the item matcher to be used for simple items, i.e. not smart filters.
     *
     * @param stack the item to be matched
     * @return an item matcher object
     */
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new SimpleItemMatcher(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(stack, world, list, flag);

        if (Minecraft.getInstance().currentScreen instanceof GuiItemRouter) {
            Slot slot = ((GuiItemRouter) Minecraft.getInstance().currentScreen).getSlotUnderMouse();
            if (slot instanceof ValidatingSlot.Module) {
                list.add(MiscUtil.translate("itemText.misc.configureHint", Keybindings.keybindConfigure.getKeyDescription()));
            }
        }
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<ITextComponent> list) {
        addSettingsInformation(stack, list);
        addAugmentInformation(stack, list);
    }

    protected void addSettingsInformation(ItemStack itemstack, List<ITextComponent> list) {
        if (isDirectional()) {
            RelativeDirection dir = ModuleHelper.getDirectionFromNBT(itemstack);
            ITextComponent itc = new TextComponentTranslation(isDirectional() && dir == RelativeDirection.NONE ?
                    "guiText.tooltip.allDirections" : "guiText.tooltip." + dir.toString());
            String dirStr = getDirectionString(dir);
            list.add(new TextComponentString(TextFormatting.YELLOW.toString()).appendSibling(itc));
        }
        addFilterInformation(itemstack, list);
        list.add(new TextComponentString(
                        TextFormatting.YELLOW + I18n.format("itemText.misc.flags") + ": " +
                                String.join(" / ",
                                        formatFlag("IGNORE_META", ModuleHelper.ignoreMeta(itemstack)),
                                        formatFlag("IGNORE_NBT", ModuleHelper.ignoreNBT(itemstack)),
                                        formatFlag("IGNORE_OREDICT", ModuleHelper.ignoreTags(itemstack)),
                                        formatFlag("TERMINATE", !ModuleHelper.terminates(itemstack))
                                )
                )
        );
        if (this instanceof IRangedModule) {
            IRangedModule rm = (IRangedModule) this;
            int curRange = rm.getCurrentRange(itemstack);
            String col = curRange > rm.getBaseRange() ?
                    TextFormatting.GREEN.toString() : curRange < rm.getBaseRange() ?
                    TextFormatting.RED.toString() : TextFormatting.AQUA.toString();
            list.add(new TextComponentString(
                    TextFormatting.YELLOW + I18n.format("itemText.misc.rangeInfo",
                            col, rm.getCurrentRange(itemstack), rm.getBaseRange(), rm.getHardMaxRange()))
            );
        }
    }

    private void addAugmentInformation(ItemStack stack, List<ITextComponent> list) {
        AugmentCounter c = new AugmentCounter(stack);
        List<ITextComponent> toAdd = Lists.newArrayList();
        for (ItemAugment augment : c.getAugments()) {
            int n = c.getAugmentCount(augment);
            if (n > 0) {
                ItemStack augmentStack = new ItemStack(augment);
                String s = augmentStack.getDisplayName().getString();
                if (n > 1) s = n + " x " + s;
                s += TextFormatting.AQUA + augment.getExtraInfo(n, stack);
                toAdd.add(new TextComponentString(" \u2022 " + TextFormatting.DARK_GREEN + s));
            }
        }
        if (!toAdd.isEmpty()) {
            list.add(new TextComponentString(TextFormatting.GREEN.toString()).appendSibling(new TextComponentTranslation("itemText.augments")));
            list.addAll(toAdd);
        }

        // TODO
    }

    public String getDirectionString(RelativeDirection dir) {
        return isOmniDirectional() && dir == RelativeDirection.NONE ?
                I18n.format("guiText.tooltip.allDirections") :
                I18n.format("guiText.tooltip." + dir.toString());
    }

    private String formatFlag(String key, boolean flag) {
        String text = I18n.format("itemText.misc." + key);
        return (flag ? TextFormatting.DARK_AQUA + TextFormatting.STRIKETHROUGH.toString() : TextFormatting.AQUA) + text + TextFormatting.RESET;
    }

    private void addFilterInformation(ItemStack itemstack, List<ITextComponent> list) {
        NBTTagList filterItems = ModuleHelper.getFilterItems(itemstack);
        list.add(new TextComponentString(
                TextFormatting.YELLOW + I18n.format("itemText.misc." + (ModuleHelper.isBlacklist(itemstack) ? "blacklist" : "whitelist")) + ":")
        );
        if (!filterItems.isEmpty()) {
            for (int i = 0; i < filterItems.size(); i++) {
                ItemStack s = ItemStack.read(filterItems.getCompound(i));
                if (s.getItem() instanceof ItemSmartFilter) {
                    int size = ((ItemSmartFilter) s.getItem()).getSize(s);
                    String suffix = size > 0 ? " [" + size + "]" : "";
                    list.add(new TextComponentString(" \u2022 " + TextFormatting.AQUA + TextFormatting.ITALIC + s.getDisplayName() + suffix));
                } else {
                    list.add(new TextComponentString(" \u2022 " + TextFormatting.AQUA + s.getDisplayName()));
                }
            }
        } else {
            ITextComponent c = list.get(list.size() - 1);
            list.set(list.size() - 1, c.appendSibling(new TextComponentString(" " + TextFormatting.AQUA + TextFormatting.ITALIC + I18n.format("itemText.misc.noItems"))));
        }
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ModuleHelper.validateNBT(stack);
        if (!player.isSneaking()) {
            if (!world.isRemote) {
                int guiId = hand == EnumHand.MAIN_HAND ? ModularRouters.GUI_MODULE_HELD_MAIN : ModularRouters.GUI_MODULE_HELD_OFF;
                NetworkHooks.openGui((EntityPlayerMP)player, new ContainerProvider(hand),
                        buf -> buf.writeBoolean(hand == EnumHand.MAIN_HAND));
            }
        } else {
            return onSneakRightClick(stack, world, player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public ActionResult<ItemStack> onSneakRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

//    @Override
//    public EnumActionResult onItemUse(ItemUseContext ctx) {
//        ItemStack stack = ctx.getItem();
//        EntityPlayer player = ctx.getPlayer();
//
//        TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(ctx.getWorld(), ctx.getPos());
//        if (router != null) {
//            if (!player.isSneaking()) {
//                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
//                buf.writeBoolean(player.getActiveHand() == EnumHand.MAIN_HAND);
//                NetworkHooks.openGui((EntityPlayerMP) player, new ContainerProvider(player.getActiveHand()), buf);
//                return EnumActionResult.SUCCESS;
//            }
//        }
//        return EnumActionResult.PASS;
//    }


    @Override
    public boolean onEntitySwing(ItemStack stack, EntityLivingBase entity) {
        // TODO handle for targeted modules
        return false;
    }

    public static class ContainerProvider extends BaseContainerProvider {
        private final EnumHand hand;
        private final BlockPos routerPos;
        private final String guiId;

        public ContainerProvider(EnumHand hand) {
            this.hand = hand;
            this.routerPos = null;
            this.guiId = "module_held";
        }

        public ContainerProvider(BlockPos routerPos) {
            this.hand = null;
            this.routerPos = routerPos;
            this.guiId = "module_installed";
        }

        @Override
        public Container createContainer(InventoryPlayer inventoryPlayer, EntityPlayer entityPlayer) {
            if (hand != null) {
                ItemStack stack = entityPlayer.getHeldItem(hand);
                if (stack.getItem() instanceof ItemModule) {
                    return ((ItemModule) stack.getItem()).createContainer(entityPlayer, hand, stack, null);
                } else {
                    return null;
                }
            } else if (routerPos != null) {
                TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(entityPlayer.getEntityWorld(), routerPos);
                int slotIndex = SlotTracker.getInstance(entityPlayer).getModuleSlot();
//                int slotIndex = router.getModuleConfigSlot(entityPlayer);
                if (slotIndex >= 0) {
                    SlotTracker.getInstance(entityPlayer).clearModuleSlot();
//                    router.clearConfigSlot(entityPlayer);
                    ItemStack installedModuleStack = router.getModules().getStackInSlot(slotIndex);
                    if (installedModuleStack.getItem() instanceof ItemModule) {
                        return ((ItemModule) installedModuleStack.getItem()).createContainer(entityPlayer, null, installedModuleStack, router);
                    } else {
                        return null;
                    }
                } else {
                    ModularRouters.LOGGER.warn("Attempt to configure module in router @ " + routerPos + " failed: router can't determine slot index!");
                }
            }
            return null;
        }

        @Override
        public String getGuiID() {
            return ModularRouters.MODID + ":" + guiId;
        }
    }

}
