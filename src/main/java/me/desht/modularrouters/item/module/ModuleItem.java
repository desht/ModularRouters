package me.desht.modularrouters.item.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.api.MRCapabilities;
import me.desht.modularrouters.api.matching.IItemMatcher;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.ClientSetup;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.container.RouterMenu;
import me.desht.modularrouters.container.handler.BaseModuleHandler.ModuleFilterHandler;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.MRBaseItem;
import me.desht.modularrouters.item.augment.AugmentItem;
import me.desht.modularrouters.item.augment.AugmentItem.AugmentCounter;
import me.desht.modularrouters.item.module.adapter.IItemAdapter;
import me.desht.modularrouters.item.module.adapter.TargetedModuleAdapter;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledModule;
import me.desht.modularrouters.logic.filter.matchers.SimpleItemMatcher;
import me.desht.modularrouters.logic.settings.ModuleFlags;
import me.desht.modularrouters.logic.settings.ModuleSettings;
import me.desht.modularrouters.logic.settings.ModuleTermination;
import me.desht.modularrouters.logic.settings.RedstoneBehaviour;
import me.desht.modularrouters.logic.settings.RelativeDirection;
import me.desht.modularrouters.util.MFLocator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static me.desht.modularrouters.client.util.ClientUtil.colorText;
import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public abstract class ModuleItem extends MRBaseItem implements ModItems.ITintable {
    private final BiFunction<ModularRouterBlockEntity, ItemStack, ? extends CompiledModule> compiler;
    private final IItemAdapter adapter;

    public ModuleItem(Properties props, BiFunction<ModularRouterBlockEntity, ItemStack, ? extends CompiledModule> compiler) {
        super(props);
        this.compiler = compiler;

        this.adapter = this instanceof ITargetedModule tm ? new TargetedModuleAdapter(tm) : new IItemAdapter.NoOp();
    }

    public static ModuleSettings getCommonSettings(ItemStack moduleStack) {
        return moduleStack.getOrDefault(ModDataComponents.COMMON_MODULE_SETTINGS, ModuleSettings.DEFAULT);
    }

    public static void setRoundRobinCounter(ItemStack moduleStack, int counter) {
        moduleStack.set(ModDataComponents.RR_COUNTER, counter);
    }

    public static int getRoundRobinCounter(ItemStack moduleStack) {
        return moduleStack.getOrDefault(ModDataComponents.RR_COUNTER, 0);
    }

    public static RedstoneBehaviour getRedstoneBehaviour(ItemStack moduleStack) {
        return getCommonSettings(moduleStack).redstoneBehaviour();
    }

    public static int getRangeModifier(ItemStack stack) {
        AugmentCounter counter = new AugmentCounter(stack);
        return counter.getAugmentCount(ModItems.RANGE_UP_AUGMENT.get()) - counter.getAugmentCount(ModItems.RANGE_DOWN_AUGMENT.get());
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
        return MRCapabilities.getMatcherProvider(stack)
                .map(p -> p.getMatcher(stack))
                .orElse(new SimpleItemMatcher(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, context, list, flag);

        if (ClientUtil.getHoveredSlot() instanceof RouterMenu.InstalledModuleSlot && !ClientUtil.isKeyDown(ClientSetup.keybindModuleInfo)) {
            Component key = ClientSetup.keybindConfigure.getKey().getDisplayName().copy().withStyle(ChatFormatting.DARK_AQUA);
            Component middleClick = Component.literal("Middle-Click").withStyle(ChatFormatting.DARK_AQUA);
            list.add(xlate("modularrouters.itemText.misc.configureHint", key, middleClick).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    protected void addUsageInformation(ItemStack itemstack, List<Component> list) {
        super.addUsageInformation(itemstack, list);
        adapter.addUsageInformation(itemstack, list);
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        addSettingsInformation(stack, list);
        addAugmentInformation(stack, list);
    }

    protected void addSettingsInformation(ItemStack stack, List<Component> list) {
        ModuleSettings settings = getCommonSettings(stack);

        if (isDirectional()) {
            MutableComponent dirStr = getDirectionString(settings.facing());
            list.add(xlate("modularrouters.guiText.label.direction").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(": "))
                    .append(dirStr.withStyle(ChatFormatting.AQUA)));
        }
        addFilterInformation(stack, list);

        ModuleFlags flags = ModuleFlags.forItem(stack);

        Component flagText = formatFlag("match_damage", flags.matchDamage())
                .append(" | ")
                .append(formatFlag("match_components", flags.matchComponents()))
                .append(" | ")
                .append(formatFlag("match_item_tags", flags.matchItemTags()));
        list.add(xlate("modularrouters.itemText.misc.flags").withStyle(ChatFormatting.YELLOW).append(": ").append(flagText));

        list.add(xlate("modularrouters.itemText.misc.match").withStyle(ChatFormatting.YELLOW)
                .append(": ")
                .append(xlate("modularrouters.itemText.misc." + (flags.matchAllItems() ? "matchAll" : "matchAny"))
                        .withStyle(ChatFormatting.AQUA)));

        if (this instanceof IRangedModule rm) {
            int curRange = rm.getCurrentRange(stack);
            ChatFormatting col = curRange > rm.getBaseRange() ?
                    ChatFormatting.GREEN : curRange < rm.getBaseRange() ?
                    ChatFormatting.RED : ChatFormatting.AQUA;
            list.add(xlate("modularrouters.itemText.misc.rangeInfo",
                    colorText(rm.getCurrentRange(stack), col),
                    colorText(rm.getBaseRange(), ChatFormatting.AQUA),
                    colorText(rm.getHardMaxRange(), ChatFormatting.AQUA)
            ).withStyle(ChatFormatting.YELLOW));
        }

        ModuleTermination termination = settings.termination();
        if (termination != ModuleTermination.NONE) {
            list.add(xlate(termination.getTranslationKey() + ".header").withStyle(ChatFormatting.YELLOW));
        }

        if (this instanceof IPickaxeUser pickaxeUser) {
            ItemStack pick = pickaxeUser.getPickaxe(stack);
            list.add(xlate("modularrouters.itemText.misc.breakerPick").withStyle(ChatFormatting.YELLOW)
                    .append(pick.getHoverName().plainCopy().withStyle(ChatFormatting.AQUA)));
            pick.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet().forEach(holder -> {
                list.add(Component.literal("▶ ")
                        .append(Enchantment.getFullname(holder.getKey(), holder.getIntValue()).copy().withStyle(ChatFormatting.AQUA))
                        .withStyle(ChatFormatting.YELLOW));
            });
        }

        int energy = getEnergyCost(stack);
        if (energy != 0) {
            list.add(xlate("modularrouters.itemText.misc.energyUsage", colorText(energy, ChatFormatting.AQUA)).withStyle(ChatFormatting.YELLOW));
        }

        adapter.addSettingsInformation(stack, list);
    }

    public abstract int getEnergyCost(ItemStack stack);

    private void addAugmentInformation(ItemStack stack, List<Component> list) {
        AugmentCounter c = new AugmentCounter(stack);
        List<Component> toAdd = Lists.newArrayList();
        for (AugmentItem augment : c.getAugments()) {
            int n = c.getAugmentCount(augment);
            if (n > 0) {
                ItemStack augmentStack = new ItemStack(augment);
                Component comp = Component.literal(" • ").withStyle(ChatFormatting.DARK_GREEN)
                        .append(n > 1 ? Component.literal(n + " x ").append(augmentStack.getHoverName()) : augmentStack.getHoverName())
                        .append(augment.getExtraInfo(n, stack).copy().withStyle(ChatFormatting.DARK_AQUA));
                toAdd.add(comp);
            }
        }
        if (!toAdd.isEmpty()) {
            list.add(xlate("modularrouters.itemText.augments").withStyle(ChatFormatting.GREEN));
            list.addAll(toAdd);
        }
    }

    public MutableComponent getDirectionString(RelativeDirection dir) {
        return isOmniDirectional() && dir == RelativeDirection.NONE ?
                xlate("modularrouters.guiText.tooltip.allDirections") :
                xlate(dir.getTranslationKey());
    }

    private MutableComponent formatFlag(String key, boolean flag) {
        return xlate("modularrouters.itemText.misc." + key)
                .withStyle(flag ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY);
    }

    protected Component getFilterItemDisplayName(ItemStack stack) {
        return stack.getHoverName();
    }

    protected MutableComponent itemListHeader(ItemStack itemstack) {
        boolean whiteList = ModuleFlags.forItem(itemstack).whiteList();
        return xlate("modularrouters.itemText.misc." + (whiteList ? "whitelist" : "blacklist")).withStyle(ChatFormatting.YELLOW);
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
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                MFLocator locator = MFLocator.heldModule(hand);
                player.openMenu(new ModuleMenuProvider(player, locator), locator::toNetwork);
            }
        } else {
            return onSneakRightClick(stack, world, player, hand);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var res = adapter.useOn(ctx);
        if (res != InteractionResult.PASS) return res;
        return super.useOn(ctx);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.getItem() instanceof IPickaxeUser pickaxeUser) {
            ItemStack pick = pickaxeUser.getPickaxe(stack);
            return !pick.isEmpty() && EnchantmentHelper.hasAnyEnchantments(pick);
        }
        return false;
    }

    public String getRegulatorTranslationKey(ItemStack stack) {
        return "modularrouters.guiText.tooltip.regulator.label";
    }

    public InteractionResultHolder<ItemStack> onSneakRightClick(ItemStack stack, Level world, Player player, InteractionHand hand) {
        return adapter.onSneakRightClick(stack, world, player, hand);
    }

    /**
     * Called server-side from ValidateModuleMessage
     *
     * @param stack the module item
     * @param player the player holding the module
     */
    public void doModuleValidation(ItemStack stack, ServerPlayer player) {
        adapter.doModuleValidation(stack, player);
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
