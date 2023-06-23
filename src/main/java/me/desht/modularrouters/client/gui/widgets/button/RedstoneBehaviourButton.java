package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class RedstoneBehaviourButton extends TexturedCyclerButton<RouterRedstoneBehaviour> {
    public RedstoneBehaviourButton(int x, int y, int width, int height, RouterRedstoneBehaviour initialVal, ISendToServer dataSyncer) {
        super(x, y, width, height, initialVal, dataSyncer);
    }

    @Override
    protected XYPoint getTextureXY() {
        return new XYPoint(16 * getState().ordinal(), 16);
    }

    @Override
    public List<Component> getTooltipLines() {
        return Collections.singletonList(
                Component.translatable("modularrouters.guiText.tooltip.redstone.label")
                        .append(Component.literal(": "))
                        .append(Component.translatable(getState().getTranslationKey()))
        );
    }
}
