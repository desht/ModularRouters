package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.core.ModMenuTypes;
import me.desht.modularrouters.item.smartfilter.SmartFilterItem;
import me.desht.modularrouters.logic.compiled.CompiledGasModule2;
import me.desht.modularrouters.logic.filter.matchers.GasMatcher;
import me.desht.modularrouters.logic.filter.matchers.IItemMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

import static me.desht.modularrouters.item.module.GasModule1.getGasContained;

public class GasModule2 extends TargetedModule implements IRangedModule, IPositionProvider {

    private static final TintColor TINT_COLOR = new TintColor(64, 224, 255);

    public GasModule2() {
        super(ModItems.defaultProps(), CompiledGasModule2::new);
    }

    @Override
    protected boolean isValidTarget(UseOnContext ctx) {
        return !ctx.getLevel().isEmptyBlock(ctx.getClickedPos());
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public MenuType<? extends ModuleMenu> getMenuType() {
        return ModMenuTypes.GAS_MENU.get();
    }

    @Override
    protected Component getFilterItemDisplayName(ItemStack stack) {
        return ItemStack.EMPTY.getDisplayName();
        //return readChemicalFromRegistry((stack.getDisplayName().
    }

    @Override
    public boolean isItemValidForFilter(ItemStack stack) {
        // only gas-holding items or a smart filter item can go into a gas module's filter
        if (stack.isEmpty() || stack.getItem() instanceof SmartFilterItem) return true;
        if (stack.getCount() > 1) return false;

        return getGasContained(stack).map(gasStack -> !gasStack.isEmpty()).orElse(false);
    }

    @Override
    public IItemMatcher getFilterItemMatcher(ItemStack stack) {
        return new GasMatcher(stack);
    }

 /*   @Override
    public boolean isGasModule() {
        return true;
    }
*/
    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.gas2BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.gas2MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    protected void addExtraInformation(ItemStack stack, List<Component> list) {
        super.addExtraInformation(stack, list);
        GasModule1.addGasModuleInformation(stack, list);
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8040E0FF;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.gasModule2EnergyCost.get();
    }

}
