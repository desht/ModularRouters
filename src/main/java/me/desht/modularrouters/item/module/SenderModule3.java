package me.desht.modularrouters.item.module;

import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule3;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SenderModule3 extends ModuleItem implements IPositionProvider, ITargetedModule {
    private static final TintColor TINT_COLOR = new TintColor(25, 255, 11);

    public SenderModule3() {
        super(ModItems.moduleProps(), CompiledSenderModule3::new);
    }

    @Override
    public boolean isRangeLimited() {
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
    public boolean canOperateInDimension(ResourceKey<Level> dimension) {
        return !ModularRouters.getDimensionBlacklist().test(dimension.location());
    }
}
