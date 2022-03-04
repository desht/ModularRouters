package me.desht.modularrouters.item.module;

import me.desht.modularrouters.client.render.area.IPositionProvider;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.compiled.CompiledSenderModule3;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

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
    public List<ModuleTarget> getStoredPositions(@Nonnull ItemStack stack) {
        ModuleTarget target = TargetedModule.getTarget(stack);
        return target == null ? Collections.emptyList() : Collections.singletonList(target);
    }

    @Override
    public int getRenderColor(int index) {
        return 0x8019FF0B;
    }

    @Override
    public int getEnergyCost(ItemStack stack) {
        return ConfigHolder.common.energyCosts.senderModule3EnergyCost.get();
    }
}
