package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class RangeUpgrade extends Upgrade {
    private final List<String> applicable = Arrays.asList(
            "sender1_module", "sender2_module", "vacuum_module", "puller2_module", "extruder_module", "extruder2_module"
    );

    @Override
    public Object[] getExtraUsageParams() {
        String s = applicable.stream().map(m -> MiscUtil.translate("item." + m + ".name")).collect(Collectors.joining(", "));
        return new String[] { s };
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGE),
                ModItems.blankUpgrade, Blocks.QUARTZ_BLOCK);
    }
}
