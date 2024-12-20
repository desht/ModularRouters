package me.desht.modularrouters.client.item;

import com.mojang.serialization.MapCodec;
import me.desht.modularrouters.core.ModItems;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static me.desht.modularrouters.util.MiscUtil.RL;

public enum ModuleTintSource implements ItemTintSource {
    INSTANCE;

    public static final ResourceLocation ID = RL("module_tint");
    public static final MapCodec<ModuleTintSource> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return stack.getItem() instanceof ModItems.ITintable tintable ? tintable.getItemTint().getRGB() : 0;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
