package me.desht.modularrouters.item.upgrade;

import com.google.common.base.Joiner;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RangeUpgrade extends Upgrade {
    private final List<String> applicable = Arrays.asList("sender1Module", "sender2Module", "vacuumModule");

    @Override
    public Object[] getExtraUsageParams() {
        List<String> l = applicable.stream().map(m -> "\u2022 " + MiscUtil.translate("item." + m + ".name")).collect(Collectors.toList());
        return new String[] { "\n" + Joiner.on("\n").join(l) };
    }

    @Override
    public IRecipe getRecipe() {
        return new ShapelessOreRecipe(ItemUpgrade.makeItemStack(ItemUpgrade.UpgradeType.RANGE),
                ModItems.blankUpgrade, Blocks.QUARTZ_BLOCK);
    }
}
