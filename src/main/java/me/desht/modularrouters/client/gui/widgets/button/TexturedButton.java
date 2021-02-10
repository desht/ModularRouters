package me.desht.modularrouters.client.gui.widgets.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.List;

public abstract class TexturedButton extends ExtendedButton implements ITooltipButton {
    static final ResourceLocation TEXTURE = new ResourceLocation(ModularRouters.MODID, "textures/gui/widgets.png");

    protected final List<ITextComponent> tooltip1;

    public TexturedButton(int x, int y, int width, int height, IPressable pressable) {
        super(x, y, width, height, StringTextComponent.EMPTY, pressable);
        this.tooltip1 = new ArrayList<>();
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getYImage(this.isHovered);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (drawStandardBackground()) {
                blit(matrixStack, this.x, this.y, i * 16, 0, this.width, this.height);
            }
            blit(matrixStack, this.x, this.y, getTextureX(), getTextureY(), this.width, this.height);
        }
    }

    protected boolean drawStandardBackground() {
        return true;
    }

    protected abstract int getTextureX();

    protected abstract int getTextureY();

    public List<ITextComponent> getTooltip() {
        return tooltip1;
    }
}
