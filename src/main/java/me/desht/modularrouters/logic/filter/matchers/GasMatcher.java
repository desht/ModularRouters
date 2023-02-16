package me.desht.modularrouters.logic.filter.matchers;

import com.google.common.collect.Sets;
import me.desht.modularrouters.item.module.GasModule1;
import me.desht.modularrouters.logic.filter.Filter;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.world.item.ItemStack;

import static me.desht.modularrouters.item.module.GasModule1.getGasContained;
import static me.desht.modularrouters.util.MiscUtil.gasTags;

public class GasMatcher implements IItemMatcher {
    private final GasStack gas;

    public GasMatcher(ItemStack stack) {
        gas = getGasContained(stack).orElse(GasStack.EMPTY);
    }

    @Override
    public boolean matchItem(ItemStack stack, Filter.Flags flags) {
        return GasModule1.getGasContained(stack)
                .map(gasStack -> matchGas(gasStack.getRaw().getChemical(), flags))
                .orElse(false);
    }

    @Override
    public boolean matchGas(Gas gas, Filter.Flags flags) {
        return gas == this.gas.getRaw() || flags.matchTags() && !Sets.intersection(gasTags(gas), gasTags(this.gas.getRaw())).isEmpty();
    }

}
