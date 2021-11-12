package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledCreativeModule;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class CreativeModule extends ItemModule {
    private static final TintColor TINT_COLOR = new TintColor(187, 38, 185);

    public CreativeModule() {
        super(ModItems.defaultProps(), CompiledCreativeModule::new);
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return 0;
    }

    @Override
    protected IFormattableTextComponent itemListHeader(ItemStack itemstack, int filterSize) {
        return xlate("modularrouters.itemText.misc.itemList");
    }
}
