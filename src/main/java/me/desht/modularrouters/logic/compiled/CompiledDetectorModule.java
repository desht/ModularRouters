package me.desht.modularrouters.logic.compiled;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.item.module.DetectorModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class CompiledDetectorModule extends CompiledModule {
    private final DetectorSettings settings;

    public CompiledDetectorModule(ModularRouterBlockEntity router, ItemStack stack) {
        super(router, stack);

        settings = stack.getOrDefault(ModDataComponents.DETECTOR_SETTINGS, DetectorSettings.DEFAULT);
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public boolean execute(@Nonnull ModularRouterBlockEntity router) {
        ItemStack stack = router.getBufferItemStack();

        if (!getFilter().test(stack)) {
            return false;
        }

        router.emitRedstone(getDirection(), getSignalLevel(), DetectorModule.SignalType.getType(isStrongSignal()));

        return true;
    }

    public int getSignalLevel() {
        return settings.signalLevel;
    }

    public boolean isStrongSignal() {
        return settings.strongSignal;
    }

    @Override
    public void onCompiled(ModularRouterBlockEntity router) {
        super.onCompiled(router);
        router.setAllowRedstoneEmission(true);
    }

    @Override
    public void cleanup(ModularRouterBlockEntity router) {
        super.cleanup(router);
        router.setAllowRedstoneEmission(false);
    }

    public record DetectorSettings(int signalLevel, boolean strongSignal) {
        public static final DetectorSettings DEFAULT = new DetectorSettings(15, false);

        public static final Codec<DetectorSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ExtraCodecs.intRange(0, 15).optionalFieldOf("signal_level", 15).forGetter(DetectorSettings::signalLevel),
                Codec.BOOL.optionalFieldOf("strong", false).forGetter(DetectorSettings::strongSignal)
        ).apply(builder, DetectorSettings::new));

        public static final StreamCodec<FriendlyByteBuf,DetectorSettings> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, DetectorSettings::signalLevel,
                ByteBufCodecs.BOOL, DetectorSettings::strongSignal,
                DetectorSettings::new
        );
    }
}
