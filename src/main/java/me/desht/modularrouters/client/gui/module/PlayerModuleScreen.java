package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.client.gui.widgets.button.TexturedCyclerButton;
import me.desht.modularrouters.client.util.XYPoint;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModBlocks;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule.Section;
import me.desht.modularrouters.logic.settings.TransferDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class PlayerModuleScreen extends ModuleScreen {
    private static final ItemStack MAIN_INV_STACK = new ItemStack(Blocks.CHEST);
    private static final ItemStack MAIN_NO_HOTBAR_INV_STACK = new ItemStack(Blocks.BARREL);
    private static final ItemStack ARMOUR_STACK = new ItemStack(Items.DIAMOND_CHESTPLATE);
    private static final ItemStack OFFHAND_STACK = new ItemStack(Items.SHIELD);
    private static final ItemStack ENDER_STACK = new ItemStack(Blocks.ENDER_CHEST);
    private static final ItemStack ROUTER_STACK = new ItemStack(ModBlocks.MODULAR_ROUTER.get());

    private static final ItemStack[] STACKS = new ItemStack[] {
            MAIN_INV_STACK, MAIN_NO_HOTBAR_INV_STACK, ARMOUR_STACK, OFFHAND_STACK, ENDER_STACK
    };

    private ItemStackCyclerButton<Section> secButton;
    private TransferDirectionButton dirButton;

    public PlayerModuleScreen(ModuleMenu container, Inventory inv, Component displayName) {
        super(container, inv, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledPlayerModule.PlayerSettings settings = moduleItemStack.get(ModDataComponents.PLAYER_SETTINGS);
        addRenderableWidget(secButton = new ItemStackCyclerButton<>(leftPos + 169, topPos + 32, 16, 16, true, STACKS, settings.section(), this));
        addRenderableWidget(dirButton = new TransferDirectionButton(leftPos + 148, topPos + 32, settings.direction()));

        getMouseOverHelp().addHelpRegion(leftPos + 127, topPos + 29, leftPos + 187, topPos + 50, "modularrouters.guiText.popup.player.control");
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        graphics.blit(RenderType::guiTextured, GUI_TEXTURE, leftPos + 167, topPos + 31, BUTTON_XY.x(), BUTTON_XY.y(), 18, 18, 256, 256);  // section "button" background

        graphics.renderItem(ROUTER_STACK, leftPos + 128, topPos + 32);
    }

    @Override
    protected void buildComponentPatch(DataComponentPatch.Builder builder) {
        super.buildComponentPatch(builder);
        builder.set(ModDataComponents.PLAYER_SETTINGS.get(), new CompiledPlayerModule.PlayerSettings(
                    dirButton.getState(),
                    secButton.getState()
            )
        );
    }

    private class TransferDirectionButton extends TexturedCyclerButton<TransferDirection> {
        TransferDirectionButton(int x, int y, TransferDirection initialVal) {
            super(x, y, 16, 16, initialVal, PlayerModuleScreen.this);
        }

        @Override
        protected XYPoint getTextureXY() {
            return new XYPoint(160 + getState().ordinal() * 16, 16);
        }
    }
}
