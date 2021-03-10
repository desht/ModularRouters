package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;

import net.minecraft.client.gui.widget.button.Button.IPressable;

class Buttons {
    static class AddButton extends TexturedButton {
        AddButton(int x, int y, IPressable pressable) {
            super(x, y, 16, 16, pressable);
        }

        @Override
        protected int getTextureX() {
            return 128;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }

    static class DeleteButton extends TexturedButton {
        private final int id;

        DeleteButton(int x, int y, int id, IPressable pressable) {
            super(x, y, 16, 16, pressable);
            this.id = id;
        }

        @Override
        protected int getTextureX() {
            return 112;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }

        public int getId() {
            return id;
        }
    }


}
