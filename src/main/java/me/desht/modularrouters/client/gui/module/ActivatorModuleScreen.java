package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActionType;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.EntityMode;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.LookDirection;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ActivatorModuleScreen extends AbstractModuleScreen {
    private static final ItemStack ITEM_STACK = new ItemStack(Items.FLINT_AND_STEEL);
    private static final ItemStack ENTITY_STACK = new ItemStack(Items.PLAYER_HEAD);
    private static final ItemStack ATTACK_STACK = new ItemStack(Items.IRON_SWORD);

    private LookDirectionButton lookDirectionButton;
    private ActionTypeButton actionTypeButton;
    private EntityModeButton entityModeButton;
    private SneakButton sneakButton;

    public ActivatorModuleScreen(ContainerModule container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledActivatorModule cam = new CompiledActivatorModule(null, moduleItemStack);

        ItemStack[] stacks = new ItemStack[] { ITEM_STACK, ENTITY_STACK, ATTACK_STACK };
        addRenderableWidget(actionTypeButton = new ActionTypeButton(leftPos + 167, topPos + 20, 16, 16, true, stacks, cam.getActionType()));
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
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        this.blit(matrixStack, leftPos + 165, topPos + 19, BUTTON_XY.x, BUTTON_XY.y, 18, 18);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        font.draw(matrixStack, I18n.get("modularrouters.guiText.tooltip.activator.action"), 132, 23, 0x404040);
        font.draw(matrixStack, I18n.get("modularrouters.guiText.tooltip.activator.sneak"), 132, 43, 0x404040);
        if (actionTypeButton.getState().isEntityTarget()) {
            font.draw(matrixStack, I18n.get("modularrouters.guiText.tooltip.activator.entityMode"), 132, 63, 0x404040);
        } else {
            font.draw(matrixStack, I18n.get("modularrouters.guiText.tooltip.activator.lookDirection"), 132, 63, 0x404040);
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
        CompoundTag compound = super.buildMessageData();
        compound.putInt(CompiledActivatorModule.NBT_ACTION_TYPE, actionTypeButton.getState().ordinal());
        compound.putInt(CompiledActivatorModule.NBT_LOOK_DIRECTION, lookDirectionButton.getState().ordinal());
        compound.putInt(CompiledActivatorModule.NBT_ENTITY_MODE, entityModeButton.getState().ordinal());
        compound.putBoolean(CompiledActivatorModule.NBT_SNEAKING, sneakButton.isToggled());
        return compound;
    }

    private class ActionTypeButton extends ItemStackCyclerButton<ActionType> {
        private final Map<ActionType, List<Component>> tooltips = new EnumMap<>(ActionType.class);

        ActionTypeButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, ActionType initialVal) {
            super(x, y, width, height, flat, stacks, initialVal, ActivatorModuleScreen.this);

            for (ActionType actionType : ActionType.values()) {
                tooltips.put(actionType, Collections.singletonList(ClientUtil.xlate(actionType.getTranslationKey())));
            }
        }

        @Override
        public List<Component> getTooltip() {
            return tooltips.get(getState());
        }
    }

    private class LookDirectionButton extends TexturedCyclerButton<LookDirection> {
        private final Map<LookDirection, List<Component>> tooltips = new EnumMap<>(LookDirection.class);

        LookDirectionButton(int x, int y, int width, int height, LookDirection initialVal) {
            super(x, y, width, height, initialVal, ActivatorModuleScreen.this);
            for (LookDirection dir : LookDirection.values()) {
                tooltips.put(dir, Collections.singletonList(ClientUtil.xlate(dir.getTranslationKey())));
            }
        }

        @Override
        protected int getTextureX() {
            return 144 + getState().ordinal() * 16;
        }

        @Override
        protected int getTextureY() {
            return 0;
        }

        @Override
        public List<Component> getTooltip() {
            return tooltips.get(getState());
        }
    }

    private class SneakButton extends TexturedToggleButton {
        SneakButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, ActivatorModuleScreen.this);
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 192 : 112;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }
    }

    private class EntityModeButton extends TexturedCyclerButton<EntityMode> {
        private final List<List<Component>> tooltips = Lists.newArrayList();

        EntityModeButton(int x, int y, int width, int height, EntityMode initialVal) {
            super(x, y, width, height, initialVal, ActivatorModuleScreen.this);
            for (EntityMode mode : EntityMode.values()) {
                tooltips.add(Collections.singletonList(ClientUtil.xlate(mode.getTranslationKey())));
            }
        }

        @Override
        protected int getTextureX() {
            switch (getState()) {
                case RANDOM: return 176;
                case NEAREST: return 192;
                case ROUND_ROBIN: return 160;
                default: return 0;
            }
        }

        @Override
        protected int getTextureY() {
            switch (getState()) {
                case RANDOM: case ROUND_ROBIN: return 32;
                case NEAREST: return 16;
                default: return 0;
            }
        }

        @Override
        public List<Component> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }
}
