package me.desht.modularrouters.gui.filter;

import me.desht.modularrouters.gui.widgets.TexturedButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.resources.I18n;

class Buttons {
    static class AddButton extends TexturedButton {
        public AddButton(int buttonId, int x, int y) {
            super(buttonId, x, y, 16, 16);
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
        public DeleteButton(int buttonId, int x, int y) {
            super(buttonId, x, y, 16, 16);
        }

        @Override
        protected int getTextureX() {
            return 112;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }

    static class ClearButton extends DeleteButton {
        ClearButton(int buttonId, int x, int y) {
            super(buttonId, x, y);
            tooltip1.add(I18n.format("guiText.tooltip.clearFilter"));
        }
    }

    static class MergeButton extends AddButton {
        MergeButton(int buttonId, int x, int y, String locStr, String name) {
            super(buttonId, x, y);
            MiscUtil.appendMultiline(tooltip1, "guiText.tooltip.mergeFilter", name, locStr);
        }
    }

    static class LoadButton extends TexturedButton {
        LoadButton(int buttonId, int x, int y, String locStr, String name) {
            super(buttonId, x, y, 16, 16);
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
