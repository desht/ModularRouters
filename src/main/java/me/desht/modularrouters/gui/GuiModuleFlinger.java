package me.desht.modularrouters.gui;

import com.google.common.collect.Lists;
import me.desht.modularrouters.container.ModuleContainer;
import me.desht.modularrouters.gui.widgets.GuiContainerBase;
import me.desht.modularrouters.gui.widgets.TextFieldWidget;
import me.desht.modularrouters.gui.widgets.TexturedButton;
import me.desht.modularrouters.logic.CompiledFlingerModuleSettings;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class GuiModuleFlinger extends GuiModule implements GuiPageButtonList.GuiResponder {
    private static final int TOOLTIP_BUTTON_BASE = 100;
    private static final int FIELD_SPEED = 0;
    private static final int FIELD_PITCH = 1;
    private static final int FIELD_YAW = 2;

    private float speed;
    private float pitch;
    private float yaw;

    public GuiModuleFlinger(ModuleContainer containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleFlinger(ModuleContainer containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledFlingerModuleSettings fs = new CompiledFlingerModuleSettings(moduleItemStack);
        speed = fs.getSpeed();
        pitch = fs.getPitch();
        yaw = fs.getYaw();
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_BASE, guiLeft + 130, guiTop + 15, 16, 16, "speed"));
        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_BASE + 1, guiLeft + 130, guiTop + 33, 16, 16, "pitch"));
        buttonList.add(new TooltipButton(TOOLTIP_BUTTON_BASE + 2, guiLeft + 130, guiTop + 51, 16, 16, "yaw"));

        FloatField t1 = new FloatField(this, FIELD_SPEED, fontRendererObj, guiLeft + 148, guiTop + 17, 35, 12, 0.0f, 20.0f);
        t1.setText(Float.toString(speed));
        t1.setGuiResponder(this);
        t1.setIncr(0.1f, 1.0f, 10.0f);
        addTextField(t1);

        FloatField t2 = new FloatField(this, FIELD_PITCH, fontRendererObj, guiLeft + 148, guiTop + 35, 35, 12, -90.0f, 90.0f);
        t2.setText(Float.toString(pitch));
        t2.setGuiResponder(this);
        addTextField(t2);

        FloatField t3 = new FloatField(this, FIELD_YAW, fontRendererObj, guiLeft + 148, guiTop + 53, 35, 12, -60.0f, 60.0f);
        t3.setText(Float.toString(yaw));
        t3.setGuiResponder(this);
        addTextField(t3);

        focus(FIELD_SPEED);
    }

    @Override
    public void setEntryValue(int id, boolean value) {
    }

    @Override
    public void setEntryValue(int id, float value) {
    }

    @Override
    public void setEntryValue(int id, String value) {
        switch (id) {
            case FIELD_SPEED: speed = parse(value); break;
            case FIELD_PITCH: pitch = parse(value); break;
            case FIELD_YAW: yaw = parse(value); break;
        }
        sendModuleSettingsDelayed(5);
    }

    private float parse(String s) {
        if (s.isEmpty() || s.equals("-")) return 0.0f;
        return Float.parseFloat(s);
    }

    @Override
    protected NBTTagCompound getExtMessageData() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat(CompiledFlingerModuleSettings.NBT_SPEED, speed);
        compound.setFloat(CompiledFlingerModuleSettings.NBT_PITCH, pitch);
        compound.setFloat(CompiledFlingerModuleSettings.NBT_YAW, yaw);
        return compound;
    }

    private static class TooltipButton extends TexturedButton {
        private final String key;

        public TooltipButton(int buttonId, int x, int y, int width, int height, String key) {
            super(buttonId, x, y, width, height);
            this.key = key;
        }

        @Override
        protected int getTextureX() {
            return 48 + 16 * (id - TOOLTIP_BUTTON_BASE);
        }

        @Override
        protected int getTextureY() {
            return 0;
        }

        @Override
        public List<String> getTooltip() {
            List<String> res = Lists.newArrayList();
            res.add(I18n.format("guiText.tooltip.flinger." + key));
            MiscUtil.appendMultiline(res, "guiText.tooltip.flinger.common");
            return res;
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
            // no click sound
        }
    }

    private static class FloatField extends TextFieldWidget {
        private final float min;
        private final float max;
        private float incr = 1.0f;
        private float fine = 0.1f;
        private float coarse = 5.0f;

        public FloatField(GuiContainerBase parent, int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height, float min, float max) {
            super(parent, componentId, fontrendererObj, x, y, par5Width, par6Height);
            this.min = min;
            this.max = max;

            setMaxStringLength(5);
            setValidator(input -> {
                if (input == null || input.isEmpty() || input.equals("-")) {
                    return true;  // treat as numeric zero
                }
                try {
                    float f = Float.parseFloat(input);
                    return f >= this.min && f <= this.max;
                } catch (NumberFormatException e) {
                    return false;
                }
            });
        }

        public void setIncr(float incr, float fine, float coarse) {
            this.incr = incr;
            this.fine = fine;
            this.coarse = coarse;
        }

        @Override
        public boolean textboxKeyTyped(char typedChar, int keyCode) {
            switch (keyCode) {
                case Keyboard.KEY_UP:
                    return adjustField(incr);
                case Keyboard.KEY_DOWN:
                    return adjustField(-incr);
                default:
                    return super.textboxKeyTyped(typedChar, keyCode);
            }
        }


        @Override
        public void onMouseWheel(int direction) {
            adjustField(direction > 0 ? incr : -incr);
        }

        public boolean adjustField(float adj) {
            if (GuiScreen.isShiftKeyDown()) {
                adj *= fine;
            } else if (GuiScreen.isCtrlKeyDown()) {
                adj *= coarse;
            }
            float val = Float.parseFloat(getText());
            setText("");
            writeText(String.format("%.1f", Math.max(min, Math.min(max, val + adj))));
            return true;
        }
    }
}
