package me.desht.modularrouters.client.gui.module;

import me.desht.modularrouters.client.gui.widgets.button.ItemStackCyclerButton;
import me.desht.modularrouters.container.ModuleMenu;
import me.desht.modularrouters.core.ModDataComponents;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule.MatchType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class BreakerModuleScreen extends ModuleScreen {
    private static final ItemStack BLOCK_STACK = new ItemStack(Blocks.IRON_BLOCK);
    private static final ItemStack ITEM_STACK = new ItemStack(Items.IRON_INGOT);
    private static final ItemStack[] STACKS = new ItemStack[] {
            ITEM_STACK, BLOCK_STACK
    };

    private ItemStackCyclerButton<MatchType> matchBlockButton;

    public BreakerModuleScreen(ModuleMenu container, Inventory inventory, Component displayName) {
        super(container, inventory, displayName);
    }

    @Override
    public void init() {
        super.init();

        CompiledBreakerModule cbm = new CompiledBreakerModule(null, moduleItemStack);

        addRenderableWidget(matchBlockButton = new ItemStackCyclerButton<>(leftPos + 147, topPos + 20, 20, 20, false, STACKS, cbm.getMatchType(), this));

        getMouseOverHelp().addHelpRegion(leftPos + 146, topPos + 19, leftPos + 165, topPos + 38, "modularrouters.guiText.popup.breaker.matchType");
    }

    @Override
    protected void buildComponentPatch(DataComponentPatch.Builder builder) {
        super.buildComponentPatch(builder);
        builder.set(ModDataComponents.BREAKER_SETTINGS.get(),
                new CompiledBreakerModule.BreakerSettings(matchBlockButton.getState()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        graphics.blit(RenderType::guiTextured, GUI_TEXTURE, leftPos + 147, topPos + 20, BUTTON_XY.x(), BUTTON_XY.y(), 18, 18, 256, 256);  // match type "button" background
    }
}
