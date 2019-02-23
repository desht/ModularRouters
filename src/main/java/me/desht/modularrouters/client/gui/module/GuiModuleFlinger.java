package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.gui.widgets.textfield.FloatTextField;
import me.desht.modularrouters.client.gui.widgets.textfield.TextFieldManager;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.item.module.FlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

public class GuiModuleFlinger extends GuiModule {
    private static final int FIELD_SPEED = GuiModule.EXTRA_TEXTFIELD_BASE;
    private static final int FIELD_PITCH = GuiModule.EXTRA_TEXTFIELD_BASE + 1;
    private static final int FIELD_YAW = GuiModule.EXTRA_TEXTFIELD_BASE + 2;

    private float speed;
    private float pitch;
    private float yaw;

    public GuiModuleFlinger(ContainerModule container) {
        super(container);

        CompiledFlingerModule fs = new CompiledFlingerModule(null, moduleItemStack);
        speed = fs.getSpeed();
        pitch = fs.getPitch();
        yaw = fs.getYaw();
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(new TooltipButton(GuiModule.EXTRA_BUTTON_BASE, guiLeft + 130, guiTop + 15, "speed", FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED));
        addButton(new TooltipButton(GuiModule.EXTRA_BUTTON_BASE + 1, guiLeft + 130, guiTop + 33, "pitch", FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH));
        addButton(new TooltipButton(GuiModule.EXTRA_BUTTON_BASE + 2, guiLeft + 130, guiTop + 51, "yaw", FlingerModule.MIN_YAW, FlingerModule.MAX_YAW));

        TextFieldManager manager = getOrCreateTextFieldManager();

        FloatTextField t1 = new FloatTextField(manager, FIELD_SPEED, fontRenderer, guiLeft + 152, guiTop + 19, 35, 12,
                FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED);
        t1.setPrecision(2);
        t1.setValue(speed);
        t1.setTextAcceptHandler((id, s) -> speed = parse(s));
        t1.setIncr(0.1f, 0.5f, 10.0f);
        t1.useGuiTextBackground();

        FloatTextField t2 = new FloatTextField(manager, FIELD_PITCH, fontRenderer, guiLeft + 152, guiTop + 37, 35, 12,
                FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH);
        t2.setValue(pitch);
        t2.setTextAcceptHandler((id, s) -> pitch = parse(s));
        t2.useGuiTextBackground();

        FloatTextField t3 = new FloatTextField(manager, FIELD_YAW, fontRenderer, guiLeft + 152, guiTop + 55, 35, 12,
                FlingerModule.MIN_YAW, FlingerModule.MAX_YAW);
        t3.setValue(yaw);
        t3.setTextAcceptHandler((id, s) -> yaw = parse(s));
        t3.useGuiTextBackground();

        manager.focus(0);

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 13, guiLeft + 186, guiTop + 32, "guiText.popup.flinger.speed");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 31, guiLeft + 186, guiTop + 50, "guiText.popup.flinger.pitch");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 49, guiLeft + 186, guiTop + 68, "guiText.popup.flinger.yaw");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        // super has already bound the correct texture
        this.drawTexturedModalRect(guiLeft + 148, guiTop + 16, LARGE_TEXTFIELD_XY.x, LARGE_TEXTFIELD_XY.y, 35, 14);
        this.drawTexturedModalRect(guiLeft + 148, guiTop + 34, LARGE_TEXTFIELD_XY.x, LARGE_TEXTFIELD_XY.y, 35, 14);
        this.drawTexturedModalRect(guiLeft + 148, guiTop + 52, LARGE_TEXTFIELD_XY.x, LARGE_TEXTFIELD_XY.y, 35, 14);
    }

    private float parse(String s) {
        if (s.isEmpty() || s.equals("-")) return 0.0f;
        return Float.parseFloat(s);
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.putFloat(CompiledFlingerModule.NBT_SPEED, speed);
        compound.putFloat(CompiledFlingerModule.NBT_PITCH, pitch);
        compound.putFloat(CompiledFlingerModule.NBT_YAW, yaw);
        return compound;
    }

    private static class TooltipButton extends TexturedButton {
        TooltipButton(int buttonId, int x, int y, String key, float min, float max) {
            super(buttonId, x, y, 16, 16);
            tooltip1.add(I18n.format("guiText.tooltip.flinger." + key, min, max));
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.numberFieldTooltip");
        }

        @Override
        protected boolean drawStandardBackground() {
            return false;
        }

        @Override
        protected int getTextureX() {
            return 48 + 16 * (id - GuiModule.EXTRA_BUTTON_BASE);
        }

        @Override
        protected int getTextureY() {
            return 0;
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no click sound
        }
    }
}
