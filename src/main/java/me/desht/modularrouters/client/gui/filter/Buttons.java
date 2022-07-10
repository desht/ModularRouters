package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.client.util.XYPoint;

import net.minecraft.client.gui.components.Button.OnPress;

class Buttons {
    static class AddButton extends TexturedButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(128, 16);

        AddButton(int x, int y, OnPress pressable) {
            super(x, y, 16, 16, pressable);
        }

        @Override
        protected XYPoint getTextureXY() {
            return TEXTURE_XY;
        }
    }

    static class DeleteButton extends TexturedButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(112, 16);
        private final int id;

        DeleteButton(int x, int y, int id, OnPress pressable) {
            super(x, y, 16, 16, pressable);
            this.id = id;
        }

        @Override
        protected XYPoint getTextureXY() {
            return TEXTURE_XY;
        }

        public int getId() {
            return id;
        }
    }


}
