package me.desht.modularrouters.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public record ModuleTargetList(@Unmodifiable List<ModuleTarget> targets) {
    public static final ModuleTargetList EMPTY = new ModuleTargetList(List.of());

    public static final Codec<ModuleTargetList> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ModuleTarget.CODEC.listOf().fieldOf("targets").forGetter(ModuleTargetList::targets)
    ).apply(builder, ModuleTargetList::new));

    public static final StreamCodec<FriendlyByteBuf,ModuleTargetList> STREAM_CODEC = StreamCodec.composite(
            ModuleTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), ModuleTargetList::targets,
            ModuleTargetList::new
    );

    public boolean isEmpty() {
        return targets.isEmpty();
    }
}
