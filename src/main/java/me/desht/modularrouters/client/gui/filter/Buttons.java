package me.desht.modularrouters.client.gui.filter;

import me.desht.modularrouters.client.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.resources.I18n;

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

    static class ClearButton extends DeleteButton {
        ClearButton(int x, int y, IPressable pressable) {
            super(x, y, 0, pressable);
            tooltip1.add(I18n.format("guiText.tooltip.clearFilter"));
        }
    }

    static class MergeButton extends AddButton {
        MergeButton(int x, int y, String locStr, String name, IPressable pressable) {
            super(x, y, pressable);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.mergeFilter", name, locStr);
        }
    }

    static class LoadButton extends TexturedButton {
        LoadButton(int x, int y, String locStr, String name,  IPressable pressable) {
            super( x, y, 16, 16, pressable);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.loadFilter", name, locStr);
        }

        @Override
        protected int getTextureX() {
            return 144;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }
}
