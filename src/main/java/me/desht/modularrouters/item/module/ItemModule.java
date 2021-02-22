package me.desht.modularrouters.item.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.gui.GuiItemRouter;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.core.ModContainerTypes;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.augment.ItemAugment.AugmentCounter;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public abstract class ItemModule extends ItemBase implements ModItems.ITintable {
    public enum ModuleFlags {
        BLACKLIST(true, 0x1, "F_blacklist"),
        IGNORE_DAMAGE(false, 0x2, "F_ignoreDamage"),
        IGNORE_NBT(true, 0x4, "F_ignoreNBT"),
        IGNORE_TAGS(true, 0x8, "F_ignoreTags");

        private final boolean defaultValue;

        private final byte mask;  // TODO legacy - remove in 1.17
        private final String name;

        ModuleFlags(boolean defaultValue, int mask, String name) {
            this.defaultValue = defaultValue;
            this.mask = (byte) mask;
            this.name = name;
        }

        public boolean getDefaultValue() {
            return defaultValue;
        }

        public byte getMask() {
            return mask;
        }

        public String getName() {
            return name;
        }
    }

    // Direction relative to the facing of the router this module is installed in
    public enum RelativeDirection {
        NONE(0x00),
        DOWN(0x01),
        UP(0x02),
        LEFT(0x04),
        RIGHT(0x08),
        FRONT(0x10),
        BACK(0x20);

        private final int mask;
        RelativeDirection(int mask) {
            this.mask = mask;
        }

        public Direction toAbsolute(Direction current) {
            switch (this) {
                case UP:
                    return Direction.UP;
                case DOWN:
                    return Direction.DOWN;
                case LEFT:
                    return current.rotateY();
                case BACK:
                    return current.getOpposite();
                case RIGHT:
                    return current.rotateYCCW();
                default: // including FRONT
                    return current;
            }
        }

        public int getMask() {
            return mask;
        }
    }

    public enum Termination {
        NONE,
        RAN,
        NOT_RAN;

        public String getTranslationKey() {
            return "modularrouters.guiText.tooltip.terminate." + toString();
        }
    }

    final BiFunction<TileEntityItemRouter, ItemStack, ? extends CompiledModule> compiler;

    public ItemModule(Properties props, BiFunction<TileEntityItemRouter, ItemStack, ? extends CompiledModule> compiler) {
        super(props);
        this.compiler = compiler;
    }

    final public CompiledModule compile(TileEntityItemRouter router, ItemStack stack) {
        return compiler.apply(router, stack);
    }

    public abstract TintColor getItemTint();

    public boolean isDirectional() {
        return true;
    }

    public boolean isOmniDirectional() { return false; }

    public boolean isFluidModule() {
        return false;
    }

    ContainerModule createContainer(int windowId, PlayerInventory invPlayer, MFLocator loc) {
        return new ContainerModule(getContainerType(), windowId, invPlayer, loc);
    }

    /**
     * Override this for any module which has a GUI providing any extra controls.  Such GUI's need their own
     * container type due to the way 1.14 handles container -> GUI connection.
     *
     * @return the container type
     */
    public ContainerType<? extends ContainerModule> getContainerType() {
        return ModContainerTypes.CONTAINER_MODULE_BASIC.get();
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
    @Nonnull
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new SimpleItemMatcher(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(stack, world, list, flag);

        TileEntityItemRouter router = ClientUtil.getOpenItemRouter();
        if (router != null) {
            Slot slot = ((GuiItemRouter) Minecraft.getInstance().currentScreen).getSlotUnderMouse();
            if (slot instanceof ContainerItemRouter.InstalledModuleSlot) {
                String s = ClientSetup.keybindConfigure.getKey().getTranslationKey();
                list.add(xlate("modularrouters.itemText.misc.configureHint", s.charAt(s.length() - 1)));
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
            RelativeDirection dir = ModuleHelper.getRelativeDirection(itemstack);
            IFormattableTextComponent itc = xlate(isOmniDirectional() && dir == RelativeDirection.NONE ?
                    "modularrouters.guiText.tooltip.allDirections" : "modularrouters.guiText.tooltip." + dir.toString());
            list.add(xlate("modularrouters.guiText.label.direction")
                    .append(new StringTextComponent(": "))
                    .append(itc.mergeStyle(TextFormatting.AQUA)));
        }
        addFilterInformation(itemstack, list);
        list.add(new StringTextComponent(
                        I18n.format("modularrouters.itemText.misc.flags") + ": " +
                                String.join(" | ",
                                        formatFlag("IGNORE_DAMAGE", ModuleHelper.ignoreDamage(itemstack)),
                                        formatFlag("IGNORE_NBT", ModuleHelper.ignoreNBT(itemstack)),
                                        formatFlag("IGNORE_TAGS", ModuleHelper.ignoreTags(itemstack))
                                )
                )
        );

        boolean matchAll = ModuleHelper.isMatchAll(itemstack);
        list.add(xlate("modularrouters.itemText.misc.match").appendString(": ")
                .append(xlate("modularrouters.itemText.misc." + (matchAll ? "matchAll" : "matchAny"))
                        .mergeStyle(TextFormatting.AQUA)));

        if (this instanceof IRangedModule) {
            IRangedModule rm = (IRangedModule) this;
            int curRange = rm.getCurrentRange(itemstack);
            String col = curRange > rm.getBaseRange() ?
                    TextFormatting.GREEN.toString() : curRange < rm.getBaseRange() ?
                    TextFormatting.RED.toString() : TextFormatting.AQUA.toString();
            list.add(xlate("modularrouters.itemText.misc.rangeInfo", col, rm.getCurrentRange(itemstack), rm.getBaseRange(), rm.getHardMaxRange()));
        }

        Termination termination = ModuleHelper.getTermination(itemstack);
        if (termination != Termination.NONE) {
            list.add(xlate(termination.getTranslationKey() + ".header").mergeStyle(TextFormatting.YELLOW));
        }

        if (this instanceof IPickaxeUser) {
            ItemStack pick = ((IPickaxeUser) this).getPickaxe(itemstack);
            list.add(xlate("modularrouters.itemText.misc.breakerPick").append(pick.getDisplayName().copyRaw().mergeStyle(TextFormatting.AQUA)));
            EnchantmentHelper.getEnchantments(pick).forEach((ench, level) ->
                    list.add(new StringTextComponent("\u25b6 ").append(ench.getDisplayName(level).copyRaw().mergeStyle(TextFormatting.AQUA)).mergeStyle(TextFormatting.YELLOW)));
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
                toAdd.add(new StringTextComponent(" \u2022 " + TextFormatting.DARK_GREEN + s));
            }
        }
        if (!toAdd.isEmpty()) {
            list.add(new StringTextComponent(TextFormatting.GREEN.toString()).append(xlate("modularrouters.itemText.augments")));
            list.addAll(toAdd);
        }
    }

    public String getDirectionString(RelativeDirection dir) {
        return isOmniDirectional() && dir == RelativeDirection.NONE ?
                I18n.format("modularrouters.guiText.tooltip.allDirections") :
                I18n.format("modularrouters.guiText.tooltip." + dir.toString());
    }

    private String formatFlag(String key, boolean flag) {
        String text = I18n.format("modularrouters.itemText.misc." + key);
        return (flag ? TextFormatting.DARK_GRAY : TextFormatting.AQUA) + text + TextFormatting.RESET;
    }

    protected ITextComponent getFilterItemDisplayName(ItemStack stack) {
        return stack.getDisplayName();
    }

    protected IFormattableTextComponent itemListHeader(ItemStack itemstack) {
        return xlate("modularrouters.itemText.misc." + (ModuleHelper.isBlacklist(itemstack) ? "blacklist" : "whitelist"));
    }

    private void addFilterInformation(ItemStack itemstack, List<ITextComponent> list) {
        List<ITextComponent> l2 = new ArrayList<>();
        ModuleFilterHandler filterHandler = new ModuleFilterHandler(itemstack, null);
        for (int i = 0; i < filterHandler.getSlots(); i++) {
            ItemStack s = filterHandler.getStackInSlot(i);
            if (s.getItem() instanceof ItemSmartFilter) {
                int size = ((ItemSmartFilter) s.getItem()).getSize(s);
                String suffix = size > 0 ? " [" + size + "]" : "";
                l2.add(new StringTextComponent(" \u2022 ").append(s.getDisplayName().copyRaw().appendString(suffix))
                        .mergeStyle(TextFormatting.AQUA, TextFormatting.ITALIC));
            } else if (!s.isEmpty()) {
                l2.add(new StringTextComponent(" \u2022 ").append(getFilterItemDisplayName(s).copyRaw()
                        .mergeStyle(TextFormatting.AQUA)));
            }
        }
        if (l2.isEmpty()) {
            list.add(itemListHeader(itemstack).mergeStyle(TextFormatting.YELLOW).appendString(": ")
                    .append(xlate("modularrouters.itemText.misc.noItems").mergeStyle(TextFormatting.AQUA, TextFormatting.ITALIC))
            );
        } else {
            list.add(itemListHeader(itemstack).mergeStyle(TextFormatting.YELLOW).appendString(": "));
            list.addAll(l2);
        }
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ModuleHelper.validateNBT(stack);
        if (!player.isSteppingCarefully()) {
            if (!world.isRemote) {
                MFLocator locator = MFLocator.heldModule(hand);
                NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(player, locator), locator::writeBuf);
            }
        } else {
            return onSneakRightClick(stack, world, player, hand);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        if (stack.getItem() instanceof IPickaxeUser) {
            ItemStack pick = ((IPickaxeUser) stack.getItem()).getPickaxe(stack);
            return !pick.isEmpty() && !EnchantmentHelper.getEnchantments(pick).isEmpty();
        }
        return false;
    }

    public String getRegulatorTranslationKey(ItemStack stack) {
        return "modularrouters.guiText.tooltip.regulator.label";
    }

    public ActionResult<ItemStack> onSneakRightClick(ItemStack stack, World world, PlayerEntity player, Hand hand) {
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return false;
    }

    public static class ContainerProvider implements INamedContainerProvider {
        private final MFLocator loc;
        private final ItemStack moduleStack;

        public ContainerProvider(PlayerEntity player, MFLocator loc) {
            this.loc = loc;
            this.moduleStack = loc.getModuleStack(player);
        }

        @Override
        public ITextComponent getDisplayName() {
            return moduleStack.getDisplayName();
        }

        @Nullable
        @Override
        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return ((ItemModule)moduleStack.getItem()).createContainer(windowId, playerInventory, loc);
        }
    }
}
