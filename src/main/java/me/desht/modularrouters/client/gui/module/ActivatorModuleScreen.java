package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActionType;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.EntityMode;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.LookDirection;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class ActivatorModuleScreen extends AbstractModuleScreen {
    private static final ItemStack ITEM_STACK = new ItemStack(Items.FLINT_AND_STEEL);
    private static final ItemStack ENTITY_STACK = new ItemStack(Items.PLAYER_HEAD);
    private static final ItemStack ATTACK_STACK = new ItemStack(Items.IRON_SWORD);

    private LookDirectionButton lookDirectionButton;
    private ItemStackCyclerButton<ActionType> actionTypeButton;
    private EntityModeButton entityModeButton;
    private SneakButton sneakButton;

    public ActivatorModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledActivatorModule cam = new CompiledActivatorModule(null, moduleItemStack);

        ItemStack[] stacks = new ItemStack[] { ITEM_STACK, ENTITY_STACK, ATTACK_STACK };
        addRenderableWidget(actionTypeButton = new ItemStackCyclerButton<>(leftPos + 167, topPos + 20, 16, 16, false, stacks, cam.getActionType(), this));
        addRenderableWidget(sneakButton = new SneakButton(leftPos + 167, topPos + 40, cam.isSneaking()));
        addRenderableWidget(lookDirectionButton = new LookDirectionButton(leftPos + 167, topPos + 60, 16, 16, cam.getLookDirection()));
        addRenderableWidget(entityModeButton = new EntityModeButton(leftPos + 167, topPos + 60, 16, 16, cam.getEntityMode()));
        lookDirectionButton.visible = !cam.getActionType().isEntityTarget();
        entityModeButton.visible = cam.getActionType().isEntityTarget();

        getMouseOverHelp().addHelpRegion(leftPos + 130, topPos + 18, leftPos + 183, topPos + 37, "modularrouters.guiText.popup.activator.action");
        getMouseOverHelp().addHelpRegion(leftPos + 130, topPos + 39, leftPos + 183, topPos + 56, "modularrouters.guiText.popup.activator.sneak");
        getMouseOverHelp().addHelpRegion(leftPos + 130, topPos + 59, leftPos + 183, topPos + 76, "modularrouters.guiText.popup.activator.look", guiContainer -> lookDirectionButton.visible);
        getMouseOverHelp().addHelpRegion(leftPos + 130, topPos + 59, leftPos + 183, topPos + 76, "modularrouters.guiText.popup.activator.look", guiContainer -> entityModeButton.visible);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        graphics.drawString(font, xlate("modularrouters.guiText.tooltip.activator.action"), 132, 23, 0x404040, false);
        graphics.drawString(font, xlate("modularrouters.guiText.tooltip.activator.sneak"), 132, 43, 0x404040, false);
        if (actionTypeButton.getState().isEntityTarget()) {
            graphics.drawString(font, xlate("modularrouters.guiText.tooltip.activator.entityMode"), 132, 63, 0x404040, false);
        } else {
            graphics.drawString(font, xlate("modularrouters.guiText.tooltip.activator.lookDirection"), 132, 63, 0x404040, false);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        lookDirectionButton.visible = !actionTypeButton.getState().isEntityTarget();
        entityModeButton.visible = actionTypeButton.getState().isEntityTarget();
    }

    @Override
    protected CompoundTag buildMessageData() {
        return Util.make(super.buildMessageData(), tag -> {
            tag.putInt(CompiledActivatorModule.NBT_ACTION_TYPE, actionTypeButton.getState().ordinal());
            tag.putInt(CompiledActivatorModule.NBT_LOOK_DIRECTION, lookDirectionButton.getState().ordinal());
            tag.putInt(CompiledActivatorModule.NBT_ENTITY_MODE, entityModeButton.getState().ordinal());
            tag.putBoolean(CompiledActivatorModule.NBT_SNEAKING, sneakButton.isToggled());
        });
    }

    private class LookDirectionButton extends TexturedCyclerButton<LookDirection> {
        LookDirectionButton(int x, int y, int width, int height, LookDirection initialVal) {
            super(x, y, width, height, initialVal, ActivatorModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(144 + getState().ordinal() * 16, 0);
        }
    }

    private class SneakButton extends TexturedToggleButton {
        private static final XYPoint TEXTURE_XY = new XYPoint(112, 16);
        private static final XYPoint TEXTURE_XY_TOGGLED = new XYPoint(192, 16);

        SneakButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, ActivatorModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return isToggled() ? TEXTURE_XY_TOGGLED : TEXTURE_XY;
        }
    }

    private class EntityModeButton extends TexturedCyclerButton<EntityMode> {
        EntityModeButton(int x, int y, int width, int height, EntityMode initialVal) {
            super(x, y, width, height, initialVal, ActivatorModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            int x = switch (getState()) {
                case RANDOM -> 176;
                case NEAREST -> 192;
                case ROUND_ROBIN -> 160;
            };
            int y = switch (getState()) {
                case RANDOM, ROUND_ROBIN -> 32;
                case NEAREST -> 16;
            };
            return new XYPoint(x, y);
        }
    }
}
