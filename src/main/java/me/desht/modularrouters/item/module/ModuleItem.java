package me.desht.modularrouters.item.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.util.TranslatableEnum;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.container.RouterMenu;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.MRBaseItem;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.augment.AugmentItem.AugmentCounter;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.util.MFLocator;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public abstract class ModuleItem extends MRBaseItem implements ModItems.ITintable {
    public enum ModuleFlags {
        BLACKLIST(true, "F_blacklist"),
        IGNORE_DAMAGE(false, "F_ignoreDamage"),
        IGNORE_NBT(true, "F_ignoreNBT"),
        IGNORE_TAGS(true, "F_ignoreTags");

        private final boolean defaultValue;
        private final String name;

        ModuleFlags(boolean defaultValue, String name) {
            this.defaultValue = defaultValue;
            this.name = name;
        }

        public boolean getDefaultValue() {
            return defaultValue;
        }

        public String getName() {
            return name;
        }

        public int getTextureY() {
            return 32;
        }
    }

    // Direction relative to the facing of the router this module is installed in
    public enum RelativeDirection {
        NONE(0x00, " "),
        DOWN(0x01, "▼"),
        UP(0x02, "▲"),
        LEFT(0x04, "◀"),
        RIGHT(0x08, "▶"),
        FRONT(0x10, "▣"),
        BACK(0x20, "▤");

        private final int mask;
        private final String symbol;

        RelativeDirection(int mask, String symbol) {
            this.mask = mask;
            this.symbol = symbol;
        }

        public Direction toAbsolute(Direction current) {
            return switch (this) {
                case UP -> Direction.UP;
                case DOWN -> Direction.DOWN;
                case LEFT -> current.getClockWise();
                case BACK -> current.getOpposite();
                case RIGHT -> current.getCounterClockWise();
                default -> current; // including FRONT
            };
        }

        public String getSymbol() {
            return symbol;
        }

        public int getMask() {
            return mask;
        }

        public int getTextureX(boolean toggled) {
            return ordinal() * 32 + (toggled ? 16 : 0);
        }

        public int getTextureY() {
            return 48;
        }
    }

    public enum Termination implements TranslatableEnum {
        NONE,
        RAN,
        NOT_RAN;

        @Override
        public String getTranslationKey() {
            return "modularrouters.guiText.tooltip.terminate." + this;
        }
    }

    final BiFunction<ModularRouterBlockEntity, ItemStack, ? extends CompiledModule> compiler;

    public ModuleItem(Properties props, BiFunction<ModularRouterBlockEntity, ItemStack, ? extends CompiledModule> compiler) {
        super(props);
        this.compiler = compiler;
    }

    final public CompiledModule compile(ModularRouterBlockEntity router, ItemStack stack) {
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

    ModuleMenu createContainer(int windowId, Inventory invPlayer, MFLocator loc) {
        return new ModuleMenu(getMenuType(), windowId, invPlayer, loc);
    }

    /**
     * Override this for any module which has a GUI providing any extra controls.  Such GUI's need their own
     * container type due to the way 1.14+ handles container -> GUI connection.
     *
     * @return the container type
     */
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.BASE_MODULE_MENU.get();
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
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, world, list, flag);

        if (ClientUtil.getHoveredSlot() instanceof RouterMenu.InstalledModuleSlot && !ClientUtil.isKeyDown(ClientSetup.keybindModuleInfo)) {
            String s = ClientSetup.keybindConfigure.getKey().getName();
            list.add(xlate("modularrouters.itemText.misc.configureHint", s.charAt(s.length() - 1)));
        }
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        addSettingsInformation(stack, list);
        addAugmentInformation(stack, list);
    }

    protected void addSettingsInformation(ItemStack itemstack, List<Component> list) {
        if (isDirectional()) {
            RelativeDirection dir = ModuleHelper.getRelativeDirection(itemstack);
            MutableComponent itc = xlate(isOmniDirectional() && dir == RelativeDirection.NONE ?
                    "modularrouters.guiText.tooltip.allDirections" : "modularrouters.guiText.tooltip." + dir.toString());
            list.add(xlate("modularrouters.guiText.label.direction")
                    .append(Component.literal(": "))
                    .append(itc.withStyle(ChatFormatting.AQUA)));
        }
        addFilterInformation(itemstack, list);
        list.add(xlate("modularrouters.itemText.misc.flags")
                .append(": ")
                .append(String.join(" | ",
                        formatFlag("IGNORE_DAMAGE", ModuleHelper.ignoreDamage(itemstack)),
                        formatFlag("IGNORE_NBT", ModuleHelper.ignoreNBT(itemstack)),
                        formatFlag("IGNORE_TAGS", ModuleHelper.ignoreTags(itemstack))
                ))
        );

        boolean matchAll = ModuleHelper.isMatchAll(itemstack);
        list.add(xlate("modularrouters.itemText.misc.match").append(": ")
                .append(xlate("modularrouters.itemText.misc." + (matchAll ? "matchAll" : "matchAny"))
                        .withStyle(ChatFormatting.AQUA)));

        if (this instanceof IRangedModule rm) {
            int curRange = rm.getCurrentRange(itemstack);
            String col = curRange > rm.getBaseRange() ?
                    ChatFormatting.GREEN.toString() : curRange < rm.getBaseRange() ?
                    ChatFormatting.RED.toString() : ChatFormatting.AQUA.toString();
            list.add(xlate("modularrouters.itemText.misc.rangeInfo", col, rm.getCurrentRange(itemstack), rm.getBaseRange(), rm.getHardMaxRange()));
        }

        Termination termination = ModuleHelper.getTermination(itemstack);
        if (termination != Termination.NONE) {
            list.add(xlate(termination.getTranslationKey() + ".header").withStyle(ChatFormatting.YELLOW));
        }

        if (this instanceof IPickaxeUser pickaxeUser) {
            ItemStack pick = pickaxeUser.getPickaxe(itemstack);
            list.add(xlate("modularrouters.itemText.misc.breakerPick").append(pick.getHoverName().plainCopy().withStyle(ChatFormatting.AQUA)));
            EnchantmentHelper.getEnchantments(pick).forEach((ench, level) ->
                    list.add(Component.literal("▶ ").append(ench.getFullname(level).plainCopy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW)));
        }

        int energy = getEnergyCost(itemstack);
        if (energy != 0) {
            list.add(xlate("modularrouters.itemText.misc.energyUsage", energy));
        }
    }

    public abstract int getEnergyCost(ItemStack stack);

    private void addAugmentInformation(ItemStack stack, List<Component> list) {
        AugmentCounter c = new AugmentCounter(stack);
        List<Component> toAdd = Lists.newArrayList();
        for (AugmentItem augment : c.getAugments()) {
            int n = c.getAugmentCount(augment);
            if (n > 0) {
                ItemStack augmentStack = new ItemStack(augment);
                MutableComponent comp = Component.literal(" • ").withStyle(ChatFormatting.DARK_GREEN);
                comp.append(n > 1 ? Component.literal(n + " x ").append(augmentStack.getHoverName()) : augmentStack.getHoverName().copy());
                comp.append(augment.getExtraInfo(n, stack).copy().withStyle(ChatFormatting.AQUA));
                toAdd.add(comp);
            }
        }
        if (!toAdd.isEmpty()) {
            list.add(Component.literal(ChatFormatting.GREEN.toString()).append(xlate("modularrouters.itemText.augments")));
            list.addAll(toAdd);
        }
    }

    public MutableComponent getDirectionString(RelativeDirection dir) {
        return isOmniDirectional() && dir == RelativeDirection.NONE ?
                xlate("modularrouters.guiText.tooltip.allDirections") :
                xlate("modularrouters.guiText.tooltip." + dir.toString());
    }

    private String formatFlag(String key, boolean flag) {
        String text = I18n.get("modularrouters.itemText.misc." + key);
        return (flag ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA) + text + ChatFormatting.RESET;
    }

    protected Component getFilterItemDisplayName(ItemStack stack) {
        return stack.getHoverName();
    }

    protected MutableComponent itemListHeader(ItemStack itemstack) {
        return xlate("modularrouters.itemText.misc." + (ModuleHelper.isBlacklist(itemstack) ? "blacklist" : "whitelist"));
    }

    private void addFilterInformation(ItemStack itemstack, List<Component> list) {
        List<Component> l2 = new ArrayList<>();
        ModuleFilterHandler filterHandler = new ModuleFilterHandler(itemstack, null);
        for (int i = 0; i < filterHandler.getSlots(); i++) {
            ItemStack s = filterHandler.getStackInSlot(i);
            if (s.getItem() instanceof SmartFilterItem sf) {
                int size = sf.getSize(s);
                String suffix = size > 0 ? " [" + size + "]" : "";
                l2.add(Component.literal(" • ").append(s.getHoverName().plainCopy().append(suffix))
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
            } else if (!s.isEmpty()) {
                l2.add(Component.literal(" • ").append(getFilterItemDisplayName(s).plainCopy()
                        .withStyle(ChatFormatting.AQUA)));
            }
        }
        if (l2.isEmpty()) {
            list.add(itemListHeader(itemstack).withStyle(ChatFormatting.YELLOW).append(": ")
                    .append(xlate("modularrouters.itemText.misc.noItems").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
            );
        } else {
            list.add(itemListHeader(itemstack).withStyle(ChatFormatting.YELLOW).append(": "));
            list.addAll(l2);
        }
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCrouching()) {
            if (!world.isClientSide) {
                MFLocator locator = MFLocator.heldModule(hand);
                player.openMenu(new ModuleMenuProvider(player, locator), locator::writeBuf);
            }
        } else {
            return onSneakRightClick(stack, world, player, hand);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.getItem() instanceof IPickaxeUser pickaxeUser) {
            ItemStack pick = pickaxeUser.getPickaxe(stack);
            return !pick.isEmpty() && !EnchantmentHelper.getEnchantments(pick).isEmpty();
        }
        return false;
    }

    public String getRegulatorTranslationKey(ItemStack stack) {
        return "modularrouters.guiText.tooltip.regulator.label";
    }

    public InteractionResultHolder<ItemStack> onSneakRightClick(ItemStack stack, Level world, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return false;
    }

    /**
     * Called server-side from ValidateModuleMessage
     *
     * @param stack the module item
     * @param player the player holding the module
     */
    public void doModuleValidation(ItemStack stack, ServerPlayer player) {
    }

    public static class ModuleMenuProvider implements MenuProvider {
        private final MFLocator loc;
        private final ItemStack moduleStack;

        public ModuleMenuProvider(Player player, MFLocator loc) {
            this.loc = loc;
            this.moduleStack = loc.getModuleStack(player);
        }

        @Override
        public Component getDisplayName() {
            return moduleStack.getHoverName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
            return ((ModuleItem)moduleStack.getItem()).createContainer(windowId, playerInventory, loc);
        }
    }
}
