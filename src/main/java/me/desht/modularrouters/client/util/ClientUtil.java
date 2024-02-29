package me.desht.modularrouters.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.gui.ModularRouterScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class ClientUtil {
    public static Level theClientLevel() {
        return Minecraft.getInstance().level;
    }

    public static Optional<ModularRouterBlockEntity> getOpenItemRouter() {
        if (Minecraft.getInstance().screen instanceof ModularRouterScreen mrs) {
            return Optional.of(mrs.getMenu().getRouter());
        } else {
            return Optional.empty();
        }
    }

    public static MutableComponent xlate(String key, Object... args) {
        return Component.translatable(key, args);
    }

    public static Component colorText(Object text, ChatFormatting... formats) {
        return Component.literal(text.toString()).withStyle(formats);
    }

    public static boolean isInvKey(int keyCode) {
        return keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue();
    }

    public static VertexConsumer posF(VertexConsumer builder, Matrix4f posMat, Vec3 vec) {
        return builder.vertex(posMat, (float)vec.x, (float)vec.y, (float)vec.z);
    }

    public static Slot getHoveredSlot() {
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> cs) {
            return cs.getSlotUnderMouse();
        }
        return null;
    }

    public static FormattedCharSequence ellipsize(Font font, String str, int maxWidth) {
        return Language.getInstance().getVisualOrder(font.ellipsize(Component.literal(str), maxWidth));
    }

    public static boolean isKeyDown(KeyMapping key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) && key.getKeyConflictContext().isActive();
    }

    public static void setMultilineTooltip(AbstractWidget w, Component... lines) {
        Arrays.stream(lines).reduce((c1, c2) -> c1.copy().append("\n").append(c2))
                .ifPresentOrElse(c -> w.setTooltip(Tooltip.create(c)),
                        () -> w.setTooltip(null));
    }

    public static void setMultilineTooltip(AbstractWidget w, Collection<Component> lines) {
        lines.stream().reduce((c1, c2) -> c1.copy().append("\n").append(c2))
                .ifPresentOrElse(c -> w.setTooltip(Tooltip.create(c)),
                        () -> w.setTooltip(null));
    }
}
