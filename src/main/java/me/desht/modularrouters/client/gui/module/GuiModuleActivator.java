package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActionType;
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
        lookDirectionButton.visible = cam.getActionType() != ActionType.USE_ITEM_ON_ENTITY;

        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 18, guiLeft + 183, guiTop + 37, "guiText.popup.activator.action");
        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 39, guiLeft + 183, guiTop + 56, "guiText.popup.activator.sneak");
        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 59, guiLeft + 183, guiTop + 76, "guiText.popup.activator.look", guiContainer -> lookDirectionButton.visible);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        this.blit(guiLeft + 165, guiTop + 19, BUTTON_XY.x, BUTTON_XY.y, 18, 18);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        font.drawString(I18n.format("guiText.tooltip.activator.action"), 132, 23, 0x404040);
        font.drawString(I18n.format("guiText.tooltip.activator.sneak"), 132, 43, 0x404040);
        if (actionTypeButton.getState() != ActionType.USE_ITEM_ON_ENTITY) {
            font.drawString(I18n.format("guiText.tooltip.activator.lookDirection"), 132, 63, 0x404040);
        }
    }

    @Override
    public void tick() {
        super.tick();

        lookDirectionButton.visible = actionTypeButton.getState() != ActionType.USE_ITEM_ON_ENTITY;
    }

    @Override
    protected CompoundNBT buildMessageData() {
        CompoundNBT compound = super.buildMessageData();
        compound.putInt(CompiledActivatorModule.NBT_ACTION_TYPE, actionTypeButton.getState().ordinal());
        compound.putInt(CompiledActivatorModule.NBT_LOOK_DIRECTION, lookDirectionButton.getState().ordinal());
        compound.putBoolean(CompiledActivatorModule.NBT_SNEAKING, sneakButton.isToggled());
        return compound;
    }

    private class ActionTypeButton extends ItemStackCyclerButton<ActionType> {
        private final List<List<String>> tips = Lists.newArrayList();

        ActionTypeButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, ActionType initialVal) {
            super(x, y, width, height, flat, stacks, initialVal, GuiModuleActivator.this);

            for (ActionType actionType : ActionType.values()) {
                tips.add(Collections.singletonList(I18n.format("itemText.activator.action." + actionType)));
            }
        }

        @Override
        public List<String> getTooltip() {
            return tips.get(getState().ordinal());
        }
    }

    private class LookDirectionButton extends TexturedCyclerButton<LookDirection> {
        private final List<List<String>> tooltips = Lists.newArrayList();

        LookDirectionButton(int x, int y, int width, int height, LookDirection initialVal) {
            super(x, y, width, height, initialVal, GuiModuleActivator.this);
            for (LookDirection dir : LookDirection.values()) {
                tooltips.add(Collections.singletonList(I18n.format("itemText.activator.direction." + dir)));
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
        public List<String> getTooltip() {
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
}
