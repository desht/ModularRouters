package me.desht.modularrouters.client.gui.widgets.button;

import me.desht.modularrouters.client.gui.ISendToServer;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;

public class RedstoneBehaviourButton extends TexturedCyclerButton<RouterRedstoneBehaviour> {
    public RedstoneBehaviourButton(int x, int y, int width, int height, RouterRedstoneBehaviour initialVal, ISendToServer dataSyncer) {
        super(x, y, width, height, initialVal, dataSyncer);
    }

    @Override
    protected int getTextureX() {
        return 16 * getState().ordinal();
    }

    @Override
    protected int getTextureY() {
        return 16;
    }

    @Override
    public List<ITextComponent> getTooltip() {
        return Collections.singletonList(
                // TODO 1.16 func_230529_a_ = appendSibling
                new TranslationTextComponent("guiText.tooltip.redstone.label")
                        .append(new StringTextComponent(": "))
                        .append(new TranslationTextComponent(getState().getTranslationKey()))
        );
    }
}
