package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import me.desht.modularrouters.client.RenderHelper;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ObjectRegistry;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule.Operation;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule.Section;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.List;

public class GuiModulePlayer extends GuiModule {
    private static final ItemStack mainInvStack = new ItemStack(Blocks.CHEST);
    private static final ItemStack armourStack = new ItemStack(Items.DIAMOND_CHESTPLATE);
    private static final ItemStack shieldStack = new ItemStack(Items.SHIELD);
    private static final ItemStack enderStack = new ItemStack(Blocks.ENDER_CHEST);
    private static final ItemStack routerStack = new ItemStack(ObjectRegistry.ITEM_ROUTER);

    private static final int OP_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE;
    private static final int SECT_BUTTON_ID = GuiModule.EXTRA_BUTTON_BASE + 1;

    private Operation operation;
    private Section section;

    public GuiModulePlayer(ContainerModule container) {
        super(container);

        CompiledPlayerModule cpm = new CompiledPlayerModule(null, moduleItemStack);
        operation = cpm.getOperation();
        section = cpm.getSection();
    }

    @Override
    public void initGui() {
        super.initGui();

        ItemStack[] stacks = new ItemStack[] { mainInvStack, armourStack, shieldStack, enderStack };
        addButton(new SectionButton(SECT_BUTTON_ID, guiLeft + 169, guiTop + 32, 16, 16, true, stacks, section) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                section = cycle(!GuiScreen.isShiftKeyDown());
                sendModuleSettingsToServer();
            }
        });
        addButton(new OperationButton(OP_BUTTON_ID, guiLeft + 148, guiTop + 32, operation) {
            @Override
            public void onClick(double p_194829_1_, double p_194829_3_) {
                operation = cycle(!GuiScreen.isShiftKeyDown());
                sendModuleSettingsToServer();
            }
        });

        getMouseOverHelp().addHelpRegion(guiLeft + 127, guiTop + 29, guiLeft + 187, guiTop + 50, "guiText.popup.player.control");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        this.drawTexturedModalRect(guiLeft + 167, guiTop + 31, BUTTON_XY.x, BUTTON_XY.y, 18, 18);  // section "button" background

        RenderHelper.renderItemStack(mc, routerStack, guiLeft + 128, guiTop + 32, "");
    }

    @Override
    protected NBTTagCompound buildMessageData() {
        NBTTagCompound compound = super.buildMessageData();
        compound.putInt(CompiledPlayerModule.NBT_OPERATION, operation.ordinal());
        compound.putInt(CompiledPlayerModule.NBT_SECTION, section.ordinal());
        return compound;
    }

    private static class SectionButton extends ItemStackCyclerButton<Section> {
        private final List<List<String>> tips = Lists.newArrayList();

        SectionButton(int buttonId, int x, int y, int width, int height, boolean flat, ItemStack[] stacks, Section initialVal) {
            super(buttonId, x, y, width, height, flat, stacks, initialVal);
            for (Section sect : Section.values()) {
                tips.add(Collections.singletonList(I18n.format("guiText.label.playerSect." + sect)));
            }
        }

        @Override
        public List<String> getTooltip() {
            return tips.get(getState().ordinal());
        }
    }

    private static class OperationButton extends TexturedCyclerButton<Operation> {
        private final List<List<String>> tooltips = Lists.newArrayList();

        OperationButton(int buttonId, int x, int y, Operation initialVal) {
            super(buttonId, x, y, 16, 16, initialVal);

            for (Operation op : Operation.values()) {
                tooltips.add(Collections.singletonList(I18n.format("guiText.label.playerOp." + op)));
            }
        }

        @Override
        protected int getTextureX() {
            return 160 + getState().ordinal() * 16;
        }

        @Override
        protected int getTextureY() {
            return 16;
        }

        @Override
        public java.util.List<String> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }
}
