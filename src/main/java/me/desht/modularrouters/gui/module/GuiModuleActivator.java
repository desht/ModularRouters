package me.desht.modularrouters.gui.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.ActionType;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule.LookDirection;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class GuiModuleActivator extends GuiModule {
    private static final ItemStack BLOCK_STACK = new ItemStack(Blocks.DISPENSER);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.BOW);
    private static final ItemStack ENTITY_STACK = new ItemStack(Items.SKULL, 1, 3);

    private static final int ACTION_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;
    private static final int LOOK_DIRECTION_ID = GuiModule.EXTRA_BUTTON_BASE + 1;
    private static final int SNEAK_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 2;

    private ActionType actionType;
    private LookDirection lookDirection;
    private LookDirectionButton lookDirectionButton;
    private boolean isSneaking;

    public GuiModuleActivator(ContainerModule containerItem, EnumHand hand) {
        this(containerItem, null, -1, hand);
    }

    public GuiModuleActivator(ContainerModule containerItem, BlockPos routerPos, Integer slotIndex, EnumHand hand) {
        super(containerItem, routerPos, slotIndex, hand);

        CompiledActivatorModule cam = new CompiledActivatorModule(null, moduleItemStack);
        actionType = cam.getActionType();
        lookDirection = cam.getLookDirection();
        isSneaking = cam.isSneaking();
    }

    @Override
    public void initGui() {
        super.initGui();

        ItemStack[] stacks = new ItemStack[] { BLOCK_STACK, ITEM_STACK, ENTITY_STACK };
        buttonList.add(new ActionTypeButton(ACTION_BUTTON_ID, guiLeft + 167, guiTop + 20, 16, 16, true, stacks, actionType));
        buttonList.add(new SneakButton(SNEAK_BUTTON_ID, guiLeft + 167, guiTop + 40, isSneaking));
        lookDirectionButton = new LookDirectionButton(LOOK_DIRECTION_ID, guiLeft + 167, guiTop + 60, 16, 16, lookDirection);
        buttonList.add(lookDirectionButton);
        lookDirectionButton.visible = actionType != ActionType.USE_ITEM_ON_ENTITY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        this.drawTexturedModalRect(guiLeft + 165, guiTop + 19, BUTTON_XY.x, BUTTON_XY.y, 18, 18);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        fontRenderer.drawString(I18n.format("guiText.tooltip.activator.action"), 132, 23, 0x404040);
        fontRenderer.drawString(I18n.format("guiText.tooltip.activator.sneak"), 132, 43, 0x404040);
        if (actionType != ActionType.USE_ITEM_ON_ENTITY) {
            fontRenderer.drawString(I18n.format("guiText.tooltip.activator.lookDirection"), 132, 63, 0x404040);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case ACTION_BUTTON_ID:
                ActionTypeButton atb = (ActionTypeButton) button;
                actionType = atb.cycle(!GuiScreen.isShiftKeyDown());
                sendModuleSettingsToServer();
                break;
            case LOOK_DIRECTION_ID:
                LookDirectionButton ldb = (LookDirectionButton) button;
                lookDirection = ldb.cycle(!GuiScreen.isShiftKeyDown());
                sendModuleSettingsToServer();
                break;
            case SNEAK_BUTTON_ID:
                SneakButton sb = (SneakButton) button;
                sb.toggle();
                isSneaking = sb.isToggled();
                sendModuleSettingsToServer();
                break;
            default:
                super.actionPerformed(button);
                break;
        }
        lookDirectionButton.visible = actionType != ActionType.USE_ITEM_ON_ENTITY;
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.setInteger(CompiledActivatorModule.NBT_ACTION_TYPE, actionType.ordinal());
        compound.setInteger(CompiledActivatorModule.NBT_LOOK_DIRECTION, lookDirection.ordinal());
        compound.setBoolean(CompiledActivatorModule.NBT_SNEAKING, isSneaking);
        return compound;
    }

    private static class ActionTypeButton extends ItemStackCyclerButton<ActionType> {
        private final List<List<String>> tips = Lists.newArrayList();

        ActionTypeButton(int buttonId, int x, int y, int width, int height, boolean flat, ItemStack[] stacks, ActionType initialVal) {
            super(buttonId, x, y, width, height, flat, stacks, initialVal);

            for (ActionType actionType : ActionType.values()) {
                tips.add(Collections.singletonList(I18n.format("itemText.activator.action." + actionType)));
            }
        }

        @Override
        public List<String> getTooltip() {
            return tips.get(getState().ordinal());
        }
    }

    private static class LookDirectionButton extends TexturedCyclerButton<LookDirection> {
        private final List<List<String>> tooltips = Lists.newArrayList();

        LookDirectionButton(int buttonId, int x, int y, int width, int height, LookDirection initialVal) {
            super(buttonId, x, y, width, height, initialVal);
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

    private static class SneakButton extends TexturedToggleButton {

        SneakButton(int buttonId, int x, int y, boolean initialVal) {
            super(buttonId, x, y, 16, 16);
            setToggled(initialVal);
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
