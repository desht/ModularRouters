package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActionType;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.EntityMode;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.LookDirection;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

public class GuiModuleActivator extends GuiModule {
    private static final ItemStack BLOCK_STACK = new ItemStack(Blocks.DISPENSER);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.BOW);
    private static final ItemStack ENTITY_STACK = new ItemStack(Items.PLAYER_HEAD);

    private LookDirectionButton lookDirectionButton;
    private ActionTypeButton actionTypeButton;
    private EntityModeButton entityModeButton;
    private SneakButton sneakButton;

    public GuiModuleActivator(ContainerModule container, PlayerInventory inv, ITextComponent displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledActivatorModule cam = new CompiledActivatorModule(null, moduleItemStack);

        ItemStack[] stacks = new ItemStack[] { BLOCK_STACK, ITEM_STACK, ENTITY_STACK };
        addButton(actionTypeButton = new ActionTypeButton(guiLeft + 167, guiTop + 20, 16, 16, true, stacks, cam.getActionType()));
        addButton(sneakButton = new SneakButton(guiLeft + 167, guiTop + 40, cam.isSneaking()));
        addButton(lookDirectionButton = new LookDirectionButton(guiLeft + 167, guiTop + 60, 16, 16, cam.getLookDirection()));
        addButton(entityModeButton = new EntityModeButton(guiLeft + 167, guiTop + 60, 16, 16, cam.getEntityMode()));
        lookDirectionButton.visible = cam.getActionType() != ActionType.USE_ITEM_ON_ENTITY;
        entityModeButton.visible = cam.getActionType() == ActionType.USE_ITEM_ON_ENTITY;

        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 18, guiLeft + 183, guiTop + 37, "guiText.popup.activator.action");
        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 39, guiLeft + 183, guiTop + 56, "guiText.popup.activator.sneak");
        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 59, guiLeft + 183, guiTop + 76, "guiText.popup.activator.look", guiContainer -> lookDirectionButton.visible);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        this.blit(matrixStack, guiLeft + 165, guiTop + 19, BUTTON_XY.x, BUTTON_XY.y, 18, 18);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        font.drawString(matrixStack, I18n.format("guiText.tooltip.activator.action"), 132, 23, 0x404040);
        font.drawString(matrixStack, I18n.format("guiText.tooltip.activator.sneak"), 132, 43, 0x404040);
        if (actionTypeButton.getState() != ActionType.USE_ITEM_ON_ENTITY) {
            font.drawString(matrixStack, I18n.format("guiText.tooltip.activator.lookDirection"), 132, 63, 0x404040);
        } else {
            font.drawString(matrixStack, I18n.format("guiText.tooltip.activator.entityMode"), 132, 63, 0x404040);
        }
    }

    @Override
    public void tick() {
        super.tick();

        lookDirectionButton.visible = actionTypeButton.getState() != ActionType.USE_ITEM_ON_ENTITY;
        entityModeButton.visible = actionTypeButton.getState() == ActionType.USE_ITEM_ON_ENTITY;
    }

    @Override
    protected CompoundNBT buildMessageData() {
        CompoundNBT compound = super.buildMessageData();
        compound.putInt(CompiledActivatorModule.NBT_ACTION_TYPE, actionTypeButton.getState().ordinal());
        compound.putInt(CompiledActivatorModule.NBT_LOOK_DIRECTION, lookDirectionButton.getState().ordinal());
        compound.putInt(CompiledActivatorModule.NBT_ENTITY_MODE, entityModeButton.getState().ordinal());
        compound.putBoolean(CompiledActivatorModule.NBT_SNEAKING, sneakButton.isToggled());
        return compound;
    }

    private class ActionTypeButton extends ItemStackCyclerButton<ActionType> {
        private final List<List<ITextComponent>> tooltips = Lists.newArrayList();

        ActionTypeButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, ActionType initialVal) {
            super(x, y, width, height, flat, stacks, initialVal, GuiModuleActivator.this);

            for (ActionType actionType : ActionType.values()) {
                tooltips.add(Collections.singletonList(ClientUtil.xlate(actionType.getTranslationKey())));
            }
        }

        @Override
        public List<ITextComponent> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }

    private class LookDirectionButton extends TexturedCyclerButton<LookDirection> {
        private final List<List<ITextComponent>> tooltips = Lists.newArrayList();

        LookDirectionButton(int x, int y, int width, int height, LookDirection initialVal) {
            super(x, y, width, height, initialVal, GuiModuleActivator.this);
            for (LookDirection dir : LookDirection.values()) {
                tooltips.add(Collections.singletonList(ClientUtil.xlate(dir.getTranslationKey())));
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
        public List<ITextComponent> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }

    private class SneakButton extends TexturedToggleButton {
        SneakButton(int x, int y, boolean initialVal) {
            super(x, y, 16, 16, initialVal, GuiModuleActivator.this);
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
        private final List<List<ITextComponent>> tooltips = Lists.newArrayList();

        EntityModeButton(int x, int y, int width, int height, EntityMode initialVal) {
            super(x, y, width, height, initialVal, GuiModuleActivator.this);
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
        public List<ITextComponent> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }
}
