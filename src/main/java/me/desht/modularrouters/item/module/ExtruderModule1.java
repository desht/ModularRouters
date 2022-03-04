package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledExtruderModule1;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ExtruderModule1 extends ModuleItem implements IRangedModule, IPickaxeUser {

    private static final TintColor TINT_COLOR = new TintColor(227, 174, 27);

    public ExtruderModule1() {
        super(ModItems.defaultProps(), CompiledExtruderModule1::new);
    }

    @Override
    public void addSettingsInformation(ItemStack itemstack, List<Component> list) {
        super.addSettingsInformation(itemstack, list);
        list.add(ClientUtil.xlate("modularrouters.itemText.extruder.mode." + ModuleHelper.getRedstoneBehaviour(itemstack)));
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.extruderModule1EnergyCost.get();
    }

    @Override
    public int getBaseRange() {
        return ConfigHolder.common.module.extruder1BaseRange.get();
    }

    @Override
    public int getHardMaxRange() {
        return ConfigHolder.common.module.extruder1MaxRange.get();
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }
}
