package me.desht.modularrouters.client.item;

import com.mojang.serialization.MapCodec;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import me.desht.modularrouters.logic.settings.TransferDirection;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum DistributorModeProperty implements SelectItemModelProperty<TransferDirection> {
    INSTANCE;

    public static final ResourceLocation ID = MiscUtil.RL("distributor_mode");
    public static final SelectItemModelProperty.Type<DistributorModeProperty,TransferDirection> TYPE
            = SelectItemModelProperty.Type.create(MapCodec.unit(INSTANCE), TransferDirection.CODEC);

    @Override
    public @Nullable TransferDirection get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return stack.getOrDefault(ModDataComponents.DISTRIBUTOR_SETTINGS, CompiledDistributorModule.DistributorSettings.DEFAULT).direction();
    }

    @Override
    public Type<? extends SelectItemModelProperty<TransferDirection>, TransferDirection> type() {
        return TYPE;
    }
}
