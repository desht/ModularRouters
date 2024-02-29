package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SenderModule3 extends TargetedModule implements IPositionProvider {

    private static final TintColor TINT_COLOR = new TintColor(25, 255, 11);

    public SenderModule3() {
        super(ModItems.defaultProps(), CompiledSenderModule3::new);
    }

    @Override
    public boolean isDirectional() {
        return false;
    }

    @Override
    protected boolean isRangeLimited() {
        return false;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8019FF0B;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.senderModule3EnergyCost.get();
    }

    @Override
    protected boolean badDimension(ResourceLocation dimId) {
        return ModularRouters.getDimensionBlacklist().test(dimId);
    }
}
