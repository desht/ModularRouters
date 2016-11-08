package me.desht.modularrouters.gui.module;

import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.FloatTextField;
import me.desht.modularrouters.gui.widgets.TextFieldManager;
import me.desht.modularrouters.gui.widgets.TexturedButton;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class GuiModuleFlinger extends GuiModule implements GuiPageButtonList.GuiResponder {
    private static final int FIELD_SPEED = GuiModule.EXTRA_TEXTFIELD_BASE;
    private static final int FIELD_PITCH = GuiModule.EXTRA_TEXTFIELD_BASE + 1;
    private static final int FIELD_YAW = GuiModule.EXTRA_TEXTFIELD_BASE + 2;

    private float speed;
    private float pitch;
    private float yaw;

    public GuiModuleFlinger(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleFlinger(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledFlingerModule fs = new CompiledFlingerModule(null, moduleItemStack);
        speed = fs.getSpeed();
        pitch = fs.getPitch();
        yaw = fs.getYaw();
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new TooltipButton(GuiModule.EXTRA_BUTTON_BASE, guiLeft + 130, guiTop + 15, "speed"));
        buttonList.add(new TooltipButton(GuiModule.EXTRA_BUTTON_BASE + 1, guiLeft + 130, guiTop + 33, "pitch"));
        buttonList.add(new TooltipButton(GuiModule.EXTRA_BUTTON_BASE + 2, guiLeft + 130, guiTop + 51, "yaw"));

        TextFieldManager manager = getOrCreateTextFieldManager();

        FloatTextField t1 = new FloatTextField(manager, FIELD_SPEED, fontRendererObj, guiLeft + 148, guiTop + 17, 35, 12, 0.0f, 20.0f);
        t1.setValue(speed);
        t1.setGuiResponder(this);
        t1.setIncr(0.1f, 1.0f, 10.0f);

        FloatTextField t2 = new FloatTextField(manager, FIELD_PITCH, fontRendererObj, guiLeft + 148, guiTop + 35, 35, 12, -90.0f, 90.0f);
        t2.setValue(pitch);
        t2.setGuiResponder(this);

        FloatTextField t3 = new FloatTextField(manager, FIELD_YAW, fontRendererObj, guiLeft + 148, guiTop + 53, 35, 12, -60.0f, 60.0f);
        t3.setValue(yaw);
        t3.setGuiResponder(this);

        manager.focus(0);
    }

    @Override
    public void setEntryValue(int id, String value) {
        switch (id) {
            case FIELD_SPEED: speed = parse(value); break;
            case FIELD_PITCH: pitch = parse(value); break;
            case FIELD_YAW: yaw = parse(value); break;
            default: super.setEntryValue(id, value); return;
        }
        sendModuleSettingsDelayed(5);
    }

    private float parse(String s) {
        if (s.isEmpty() || s.equals("-")) return 0.0f;
        return Float.parseFloat(s);
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.setFloat(CompiledFlingerModule.NBT_SPEED, speed);
        compound.setFloat(CompiledFlingerModule.NBT_PITCH, pitch);
        compound.setFloat(CompiledFlingerModule.NBT_YAW, yaw);
        return compound;
    }

    private static class TooltipButton extends TexturedButton {
        TooltipButton(int buttonId, int x, int y, String key) {
            super(buttonId, x, y, 16, 16);
            tooltip1.add(I18n.format("guiText.tooltip.flinger." + key));
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.floatFieldTooltip");
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
