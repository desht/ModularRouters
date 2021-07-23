package me.desht.modularrouters.client.gui.module;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.util.GuiUtil;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule.Operation;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule.Section;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.List;

import static me.desht.modularrouters.client.util.ClientUtil.xlate;

public class PlayerModuleScreen extends AbstractModuleScreen {
    private static final ItemStack MAIN_INV_STACK = new ItemStack(Blocks.CHEST);
    private static final ItemStack MAIN_NO_HOTBAR_INV_STACK = new ItemStack(Blocks.BARREL);
    private static final ItemStack ARMOUR_STACK = new ItemStack(Items.DIAMOND_CHESTPLATE);
    private static final ItemStack OFFHAND_STACK = new ItemStack(Items.SHIELD);
    private static final ItemStack ENDER_STACK = new ItemStack(Blocks.ENDER_CHEST);
    private static final ItemStack ROUTER_STACK = new ItemStack(ModBlocks.MODULAR_ROUTER.get());

    private static final ItemStack[] STACKS = new ItemStack[] {
            MAIN_INV_STACK, MAIN_NO_HOTBAR_INV_STACK, ARMOUR_STACK, OFFHAND_STACK, ENDER_STACK
    };

    private SectionButton secButton;
    private OperationButton opButton;

    public PlayerModuleScreen(ContainerModule container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledPlayerModule cpm = new CompiledPlayerModule(null, moduleItemStack);

        addRenderableWidget(secButton = new SectionButton(leftPos + 169, topPos + 32, 16, 16, true, STACKS, cpm.getSection()));
        addRenderableWidget(opButton = new OperationButton(leftPos + 148, topPos + 32, cpm.getOperation()));

        getMouseOverHelp().addHelpRegion(leftPos + 127, topPos + 29, leftPos + 187, topPos + 50, "modularrouters.guiText.popup.player.control");
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        this.blit(matrixStack, leftPos + 167, topPos + 31, BUTTON_XY.x, BUTTON_XY.y, 18, 18);  // section "button" background

        GuiUtil.renderItemStack(matrixStack, minecraft, ROUTER_STACK, leftPos + 128, topPos + 32, "");
    }

    @Override
    protected CompoundTag buildMessageData() {
        CompoundTag compound = super.buildMessageData();
        compound.putInt(CompiledPlayerModule.NBT_OPERATION, opButton.getState().ordinal());
        compound.putInt(CompiledPlayerModule.NBT_SECTION, secButton.getState().ordinal());
        return compound;
    }

    private class SectionButton extends ItemStackCyclerButton<Section> {
        private final List<List<Component>> tips = Lists.newArrayList();

        SectionButton(int x, int y, int width, int height, boolean flat, ItemStack[] stacks, Section initialVal) {
            super(x, y, width, height, flat, stacks, initialVal, PlayerModuleScreen.this);
            for (Section sect : Section.values()) {
                tips.add(Collections.singletonList(xlate(sect.getTranslationKey())));
            }
        }

        @Override
        public List<Component> getTooltip() {
            return tips.get(getState().ordinal());
        }
    }

    private class OperationButton extends TexturedCyclerButton<Operation> {
        private final List<List<Component>> tooltips = Lists.newArrayList();

        OperationButton(int x, int y, Operation initialVal) {
            super(x, y, 16, 16, initialVal, PlayerModuleScreen.this);

            for (Operation op : Operation.values()) {
                tooltips.add(Collections.singletonList(xlate(op.getTranslationKey())));
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
        public List<Component> getTooltip() {
            return tooltips.get(getState().ordinal());
        }
    }
}
